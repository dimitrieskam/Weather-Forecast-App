package com.example.weatherproject.entity.exceptions;

public class FailedToFetchWeatherException extends RuntimeException{
    public FailedToFetchWeatherException(String city) {
        super(String.format("Failed to fetch weather for %s", city));
    }
}
