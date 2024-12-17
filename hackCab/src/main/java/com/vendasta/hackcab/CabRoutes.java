package com.vendasta.hackcab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import static java.lang.Math.*;

@SpringBootApplication
public class CabRoutes {
    public static void main(String[] args) {
        SpringApplication.run(CabRoutes.class, args);
    }

    // Data Model for Employee
    static class Employee {
        String name;
        double latitude;
        double longitude;

        Employee(String name, double latitude, double longitude) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    // Data Model for Cab
    static class Cab {
        List<Employee> employees = new ArrayList<>();
        double distanceToOffice;

        void addEmployee(Employee employee) {
            employees.add(employee);
        }

        boolean isFull(int maxCapacity) {
            return employees.size() >= maxCapacity;
        }

        void calculateDistanceToOffice(double officeLat, double officeLng) {
            this.distanceToOffice = employees.stream()
                    .mapToDouble(emp -> haversine(emp.latitude, emp.longitude, officeLat, officeLng))
                    .average()
                    .orElse(Double.MAX_VALUE);
        }
    }

    // Haversine Formula
    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of Earth in km
        double dLat = toRadians(lat2 - lat1);
        double dLon = toRadians(lon2 - lon1);
        double a = sin(dLat / 2) * sin(dLat / 2)
                + cos(toRadians(lat1)) * cos(toRadians(lat2))
                * sin(dLon / 2) * sin(dLon / 2);
        double c = 2 * atan2(sqrt(a), sqrt(1 - a));
        return R * c;
    }

    // Direction Classification
    public static String getDirection(double empLat, double empLng, double officeLat, double officeLng) {
        double latDiff = empLat - officeLat;
        double lngDiff = empLng - officeLng;

        if (latDiff < 0 && abs(lngDiff) < 0.01) return "South"; // Primarily south
        else if (latDiff > 0 && abs(lngDiff) < 0.01) return "North"; // Primarily north
        else if (lngDiff > 0) return "East"; // East direction
        else return "West"; // West direction
    }

    // Cab Allocation Logic with Route Optimization
    public static List<Cab> allocateCabs(List<Employee> employees, double officeLat, double officeLng, int maxCapacity) {
        // Group employees by direction
        Map<String, List<Employee>> directionGroups = new HashMap<>();
        directionGroups.put("North", new ArrayList<>());
        directionGroups.put("South", new ArrayList<>());
        directionGroups.put("East", new ArrayList<>());
        directionGroups.put("West", new ArrayList<>());

        for (Employee emp : employees) {
            String direction = getDirection(emp.latitude, emp.longitude, officeLat, officeLng);
            directionGroups.get(direction).add(emp);
        }

        // Combine and optimize cabs
        List<Cab> allCabs = new ArrayList<>();
        for (String direction : directionGroups.keySet()) {
            List<Employee> group = directionGroups.get(direction);
            while (!group.isEmpty()) {
                Cab cab = new Cab();
                Iterator<Employee> it = group.iterator();
                while (it.hasNext() && !cab.isFull(maxCapacity)) {
                    cab.addEmployee(it.next());
                    it.remove();
                }
                // Optimize drop order for this cab
                cab.employees.sort(Comparator.comparingDouble(emp -> haversine(emp.latitude, emp.longitude, officeLat, officeLng)));
                cab.calculateDistanceToOffice(officeLat, officeLng);
                allCabs.add(cab);
            }
        }
        return allCabs;
    }

    // REST Controller
    @RestController
    @RequestMapping("/api/cabs")
    @CrossOrigin(origins = "*")
    public static class CabController {
        // Office location (Ekkatuthangal)
        private final double officeLat = 13.0255;
        private final double officeLng = 80.2115;

        @PostMapping("/allocate")
        public Map<String, List<String>> allocate(@RequestBody List<Map<String, String>> employeesInput,
                                                  @RequestParam int maxCapacity) {
            // Parse input into Employee objects
            List<Employee> employees = new ArrayList<>();
            for (Map<String, String> input : employeesInput) {
                String name = input.get("name");
                String[] location = input.get("location").split(":");
                double lat = Double.parseDouble(location[0]);
                double lng = Double.parseDouble(location[1]);
                employees.add(new Employee(name, lat, lng));
            }

            // Allocate cabs
            List<Cab> cabs = allocateCabs(employees, officeLat, officeLng, maxCapacity);

            // Prepare output with "Cab 1", "Cab 2" etc.
            Map<String, List<String>> result = new LinkedHashMap<>();
            for (int i = 0; i < cabs.size(); i++) {
                Cab cab = cabs.get(i);
                List<String> cabEmployees = new ArrayList<>();
                for (Employee emp : cab.employees) {
                    cabEmployees.add(emp.name);
                }
                result.put("Cab " + (i + 1), cabEmployees);
            }

            return result;
        }
    }
}
