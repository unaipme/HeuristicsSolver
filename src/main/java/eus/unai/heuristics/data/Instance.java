package eus.unai.heuristics.data;

import eus.unai.heuristics.exception.InstanceParseException;
import lombok.Builder;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Builder
@Getter
public class Instance {

    private int nBuses;
    private int nDrivers;
    private int nServices;

    private int maxBuses;

    private int BM;
    private float CBM;
    private float CEM;

    private Bus [] buses;
    private Driver [] drivers;
    private Service [] services;

    public static Instance readFromFile(Path path) throws IOException {
            try {
                String instanceContent = removeComments(Files.readAllLines(path).stream().collect(Collectors.joining()));

                Map<String, String> data = new HashMap<>();
                Arrays.asList(instanceContent.replaceAll("\n", "").split(";")).forEach(s -> {
                    String[] keyValue = s.split("=");
                    data.put(keyValue[0].trim(), keyValue[1].trim());
                });
                int nBuses = Integer.parseInt(data.get("nBuses"));
                int nDrivers = Integer.parseInt(data.get("nDrivers"));
                int nServices = Integer.parseInt(data.get("nServices"));
                Bus[] buses = new Bus[nBuses];
                IntStream.range(0, nBuses).forEach(i ->
                        buses[i] = Bus.builder()
                                .index(i)
                                .cap(Integer.parseInt(readArray(data.get("cap"))[i]))
                                .euros_km(Float.parseFloat(readArray(data.get("euros_km"))[i]))
                                .euros_min(Float.parseFloat(readArray(data.get("euros_min"))[i]))
                                .build()
                );
                Driver[] drivers = new Driver[nDrivers];
                IntStream.range(0, nDrivers).forEach(i ->
                        drivers[i] = Driver.builder()
                                .index(i)
                                .maxHours(Integer.parseInt(readArray(data.get("maxHours"))[i]))
                                .build()
                );
                Service[] services = new Service[nServices];
                IntStream.range(0, nServices).forEach(i ->
                        services[i] = Service.builder()
                                .index(i)
                                .st(Integer.parseInt(readArray(data.get("st"))[i]))
                                .sdt(Integer.parseInt(readArray(data.get("sdt"))[i]))
                                .sdd(Integer.parseInt(readArray(data.get("sdd"))[i]))
                                .dem(Integer.parseInt(readArray(data.get("dem"))[i]))
                                .build()
                );
                return Instance.builder()
                        .nBuses(nBuses)
                        .nDrivers(nDrivers)
                        .nServices(nServices)
                        .buses(buses)
                        .drivers(drivers)
                        .services(services)
                        .maxBuses(Integer.parseInt(data.get("maxBuses")))
                        .BM(Integer.parseInt(data.get("BM")))
                        .CBM(Float.parseFloat(data.get("CBM")))
                        .CEM(Float.parseFloat(data.get("CEM")))
                        .build();
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw new InstanceParseException();
            }
    }

    private static String [] readArray(String array) {
        return array.substring(1, array.length() - 1).trim().split(" ");
    }

    private static String removeComments(String str) {
        while (str.contains("/*") || str.contains("*/")) {
            String beginStr = str.substring(0, str.indexOf("/*"));
            String endStr = str.substring(str.indexOf("*/") + 2);
            str = beginStr.concat(endStr);
        }
        return str;
    }

    public int[][] overlaps() {
        int[][] matrix = new int[nServices][nServices];
        for (int i = 0; i < nServices - 1; i++) {
            for (int j = i; j < nServices; j++) {
                Service s1 = services[i], s2 = services[j];
                if (s1.getSt() <= s2.getSt() && s2.getSt() <= s1.getSt() + s1.getSdt()) {
                    matrix[i][j] = 1;
                    matrix[j][i] = 1;
                } else {
                    matrix[i][j] = 0;
                    matrix[j][i] = 0;
                }
            }
        }
        return matrix;
    }

}