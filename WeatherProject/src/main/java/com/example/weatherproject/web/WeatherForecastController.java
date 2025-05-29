package com.example.weatherproject.web;

import com.example.weatherproject.service.WeatherForecastService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:8081"})
public class WeatherForecastController {
    private final WeatherForecastService service;

    public WeatherForecastController(WeatherForecastService service) {
        this.service = service;
    }

    @GetMapping("/load")
    public String loadWeatherData(){
        service.fetchAndStoreWeatherData();
        return "Weather data loaded successfully";
    }

    @GetMapping("/all")
    public List<Map<String, Object>> getAllForecasts(){
        return service.getAllForecasts().stream()
                .map(f->{
                    Map<String, Object> map = new HashMap<>();
                    map.put("city", f.getCity());
                    map.put("date", f.getDate());
                    map.put("maxTemperature", f.getMaxTemp());
                    map.put("feelsLike", f.getFeelsLikeTemp());

                    return map;
                }).collect(Collectors.toList());
    }

    @GetMapping("/hot-days")
    public List<Map<String, Object>> getHotDays(){
        return service.getHotDays().stream()
                .map(f-> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("city", f.getCity());
                    map.put("date", f.getDate());
                    map.put("maxTemperature", f.getMaxTemp());
                    map.put("feelsLike", f.getFeelsLikeTemp());
                    return map;
                }).collect(Collectors.toList());
    }


}
