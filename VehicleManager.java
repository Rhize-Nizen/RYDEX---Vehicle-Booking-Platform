import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class VehicleManager {

    public enum VehicleType {
        BIKE("Rydex Bike", 20.0, 7.5, 1.0),
        AUTO("CG Auto", 30.0, 11.0, 1.5),
        CAR_4_SEATER("Car 4 Seater", 50.0, 14.0, 2.0),
        CAR_7_SEATER("7 Seater", 80.0, 20.0, 3.0);

        public final String label;
        public final double baseFare;
        public final double perKmRate;
        public final double perMinRate;

        VehicleType(String label, double baseFare, double perKmRate, double perMinRate) {
            this.label = label;
            this.baseFare = baseFare;
            this.perKmRate = perKmRate;
            this.perMinRate = perMinRate;
        }
    }

    public static class Driver {
        public String id;
        public String name;
        public String phone;
        public String vehicleNumber;
        public VehicleType type;
        public double rating;
        public double lat;
        public double lng;

        public Driver(String id, String name, String phone, String vehicleNumber, VehicleType type, double rating, double lat, double lng) {
            this.id = id;
            this.name = name;
            this.phone = phone;
            this.vehicleNumber = vehicleNumber;
            this.type = type;
            this.rating = rating;
            this.lat = lat;
            this.lng = lng;
        }
    }

    public static class Booking {
        public String bookingId;
        public String vehicleType;
        public String pickup;
        public String dropoff;
        public double distanceKm;
        public double totalFare;
        public String otp;
        public Driver assignedDriver;
        public String status;

        public Booking(String id, String type, String pickup, String dropoff, double dist, double fare, String otp, Driver driver) {
            this.bookingId = id;
            this.vehicleType = type;
            this.pickup = pickup;
            this.dropoff = dropoff;
            this.distanceKm = dist;
            this.totalFare = fare;
            this.otp = otp;
            this.assignedDriver = driver;
            this.status = "CONFIRMED";
        }
    }

    private final List<Driver> driverFleet = new ArrayList<>();
    private final Map<String, Booking> activeBookings = new ConcurrentHashMap<>();

    public VehicleManager() {
        seedBhilaiDurgFleet();
    }

    private void seedBhilaiDurgFleet() {
        // CG-07 registered drivers across Bhilai & Durg
        driverFleet.add(new Driver("DRV-101", "Ramesh Verma", "+91 98271 22334", "CG 07 BH 1290", VehicleType.BIKE, 4.8, 21.2032, 81.3394));
        driverFleet.add(new Driver("DRV-102", "Aman Sahu", "+91 94060 44551", "CG 07 BA 5541", VehicleType.BIKE, 4.9, 21.2291, 81.3323));
        driverFleet.add(new Driver("DRV-103", "Vikram Patel", "+91 99931 77882", "CG 07 AT 9021", VehicleType.AUTO, 4.7, 21.1904, 81.2849));
        driverFleet.add(new Driver("DRV-104", "Santosh Dewangan", "+91 70002 11983", "CG 07 T 3312", VehicleType.AUTO, 4.6, 21.1895, 81.3533));
        driverFleet.add(new Driver("DRV-105", "Dinesh Yadav", "+91 98263 88990", "CG 07 MB 7710", VehicleType.CAR_4_SEATER, 4.8, 21.2268, 81.3090));
        driverFleet.add(new Driver("DRV-106", "Rajesh Pandey", "+91 91114 66231", "CG 07 CP 4429", VehicleType.CAR_7_SEATER, 4.9, 21.1882, 81.3210));
    }

    public double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round((R * c) * 100.0) / 100.0;
    }

    public Map<String, Object> calculateAllFares(double distanceKm) {
        double estimatedTimeMin = Math.max(5.0, distanceKm * 2.5);
        Map<String, Object> fares = new LinkedHashMap<>();

        for (VehicleType vt : VehicleType.values()) {
            double fare = vt.baseFare + (distanceKm * vt.perKmRate) + (estimatedTimeMin * vt.perMinRate);
            double totalWithTax = Math.round((fare * 1.05) * 10.0) / 10.0; // 5% GST

            Map<String, Object> details = new HashMap<>();
            details.put("label", vt.label);
            details.put("fare", totalWithTax);
            details.put("etaMinutes", (int) Math.round(estimatedTimeMin));
            fares.put(vt.name(), details);
        }
        return fares;
    }

    // Update createBooking to accept roadDistance
    public Booking createBooking(String vehicleTypeStr, String pickup, String dropoff,
                                 double pickupLat, double pickupLng,
                                 double dropLat, double dropLng, double roadDistance) {
        VehicleType type = VehicleType.valueOf(vehicleTypeStr.toUpperCase());

        // Use road distance if provided, otherwise fallback to Haversine
        double tripDistance = roadDistance > 0 ? roadDistance : calculateDistanceKm(pickupLat, pickupLng, dropLat, dropLng);
        if (tripDistance < 0.5) tripDistance = 1.0;

        Driver matchedDriver = null;
        double minDistance = Double.MAX_VALUE;

        for (Driver d : driverFleet) {
            if (d.type == type) {
                double dist = calculateDistanceKm(pickupLat, pickupLng, d.lat, d.lng);
                if (dist < minDistance) {
                    minDistance = dist;
                    matchedDriver = d;
                }
            }
        }

        if (matchedDriver == null) {
            matchedDriver = new Driver("DRV-TEMP", "Sunil Nishad", "+91 98270 00111", "CG 07 B 9999", type, 4.7, pickupLat, pickupLng);
        }

        Map<String, Object> fareData = (Map<String, Object>) calculateAllFares(tripDistance).get(type.name());
        double finalFare = (double) fareData.get("fare");
        String otp = String.format("%04d", new Random().nextInt(10000));
        String bookingId = "RYD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Booking booking = new Booking(bookingId, type.label, pickup, dropoff, tripDistance, finalFare, otp, matchedDriver);
        activeBookings.put(bookingId, booking);
        return booking;
    }

    // Overload for backward compatibility
    public Booking createBooking(String vehicleTypeStr, String pickup, String dropoff,
                                 double pickupLat, double pickupLng, double dropLat, double dropLng) {
        return createBooking(vehicleTypeStr, pickup, dropoff, pickupLat, pickupLng, dropLat, dropLng, 0);
    }

    public Booking getBooking(String bookingId) {
        return activeBookings.get(bookingId);
    }
}