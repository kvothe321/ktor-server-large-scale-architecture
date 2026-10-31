CREATE TABLE weather_cache (
    latitude            DOUBLE PRECISION NOT NULL,
    longitude           DOUBLE PRECISION NOT NULL,
    temperature_celsius DOUBLE PRECISION NOT NULL,
    wind_speed_kmh      DOUBLE PRECISION NOT NULL,
    weather_code        INTEGER          NOT NULL,
    observed_at         TIMESTAMP        NOT NULL,
    fetched_at          TIMESTAMP        NOT NULL,
    PRIMARY KEY (latitude, longitude)
);
