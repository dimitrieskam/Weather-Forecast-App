package com.example.weatherproject.service;

import com.example.weatherproject.entity.WeatherForecast;

import java.util.List;

public interface WeatherForecastService {
    void fetchAndStoreWeatherData();
    List<WeatherForecast> getHotDays();
    List<WeatherForecast> getAllForecasts();
}
