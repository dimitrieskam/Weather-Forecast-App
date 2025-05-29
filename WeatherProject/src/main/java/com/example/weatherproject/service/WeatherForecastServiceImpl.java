package com.example.weatherproject.service;


import com.example.weatherproject.entity.WeatherForecast;
import com.example.weatherproject.entity.exceptions.FailedToFetchWeatherException;
import com.example.weatherproject.repository.WeatherForecastRepository;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WeatherForecastServiceImpl implements WeatherForecastService{
    @Value("${openweather.api.key}")
    private String apiKey;

    @Value("${openweather.api.url}")
    private String apiUrl;

    @Value("${openweather.api.cities}")
    private String cities;

    private final WeatherForecastRepository weatherForecastRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public WeatherForecastServiceImpl(WeatherForecastRepository weatherForecastRepository) {
        this.weatherForecastRepository = weatherForecastRepository;
    }

    private List<CityCoordinates> getConfiguredCities(){
        return Arrays.stream(cities.split(";"))
                .map(entry->{
                    String[] parts = entry.split(":");
                    String name = parts[0];
                    String[] coords = parts[1].split(",");
                    return new CityCoordinates(name, Double.parseDouble(coords[0]), Double.parseDouble(coords[1]));
                })
                .toList();
    }

    public void fetchAndStoreWeatherData(){
        weatherForecastRepository.deleteAll();
        for(CityCoordinates city : getConfiguredCities()) {
            String url = String.format("%s?lat=%.4f&lon=%.4f&appid=%s&units=metric",
                    apiUrl, city.lat(), city.lon(), apiKey);
            try {
                ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
                JsonNode list = response.getBody().get("list");

                Map<LocalDate, Double> maxTempsPerDay = new HashMap<>();
                Map<LocalDate, Double> feelsLikePerDay = new HashMap<>();


                for (JsonNode item : list) {
                    long timestamp = item.get("dt").asLong();
                    LocalDate date = Instant.ofEpochSecond(timestamp)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();

                    double tempMax = item.get("main").get("temp_max").asDouble();
                    double feelsLike = item.get("main").get("feels_like").asDouble();


                    maxTempsPerDay.merge(date, tempMax, Math::max);
                    feelsLikePerDay.merge(date, feelsLike, Math::max);

                }

                    for (LocalDate date : maxTempsPerDay.keySet()){
                            WeatherForecast forecast = new WeatherForecast();
                            forecast.setCity(city.name());
                            forecast.setDate(date);
                            forecast.setMaxTemp(maxTempsPerDay.get(date));
                            forecast.setFeelsLikeTemp(feelsLikePerDay.get(date));
                            weatherForecastRepository.save(forecast);
                    }
            } catch (Exception e) {
                throw new FailedToFetchWeatherException(city.name());
            }
        }

    }
    public List<WeatherForecast> getHotDays(){
        return weatherForecastRepository.findByMaxTempGreaterThan(25.0);
    }

    @Override
    public List<WeatherForecast> getAllForecasts() {
        return weatherForecastRepository.findAll();
    }

    private record CityCoordinates(String name, double lat, double lon){}
}
