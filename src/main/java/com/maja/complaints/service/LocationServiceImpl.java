package com.maja.complaints.service;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CountryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

@Slf4j
@Service
public class LocationServiceImpl implements LocationService {

    private final DatabaseReader databaseReader;

    public LocationServiceImpl(@Value("${geoip.database.path}") String databasePath) throws IOException {
        File database = new File(databasePath);
        this.databaseReader = new DatabaseReader.Builder(database).build();
    }

    @Override
    public String locateOrDefault(String ipAddressString, String fallbackCountryValue) {
        InetAddress ipAddress;
        CountryResponse response = null;

        try {
            ipAddress = InetAddress.getByName(ipAddressString);
            response = databaseReader.country(ipAddress);
        } catch (UnknownHostException e) {
            log.info("Could not recognize IP address {}, falling back to {}", ipAddressString, fallbackCountryValue);
        } catch (IOException e) {
            log.warn("Could not read database, falling back to {}", fallbackCountryValue, e);
        } catch (GeoIp2Exception e) {
            log.warn("Could not read country information for IP address {}, falling back to {}", ipAddressString, fallbackCountryValue, e.getCause());
        }

        return Optional.ofNullable(response)
                .map(countryResponse -> countryResponse.getCountry().getIsoCode())
                .orElse(fallbackCountryValue);
    }
}