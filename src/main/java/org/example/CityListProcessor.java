package org.example;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class CityListProcessor {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Enter path to the file or 'exit' to leave the programm:");
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("exit")) {
                break;
            }
            processFile(input);
        }
        scanner.close();
    }

    private static void processFile(String filePath) {
        List<Address> addresses = new ArrayList<>();
        long startTime = System.currentTimeMillis();

        if (filePath.endsWith(".csv")) {
            addresses = readCSV(filePath);
        } else if (filePath.endsWith(".xml")) {
            addresses = readXML(filePath);
        } else {
            System.out.println("ERROR unsupported file format");
            return;
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        displayStatistics(addresses, duration);
    }

    private static List<Address> readCSV(String filePath) {
        List<Address> addresses = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                String[] values = line.split(";");
                Address address = new Address(
                        values[0].replaceAll("\"", ""),
                        values[1].replaceAll("\"", ""),
                        Integer.parseInt(values[2].replaceAll("\"", "")),
                        Integer.parseInt(values[3].replaceAll("\"", ""))
                );
                addresses.add(address);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return addresses;
    }

    private static List<Address> readXML(String filePath) {
        List<Address> addresses = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(filePath);
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName("item");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                Address address = new Address(
                        element.getAttribute("city"),
                        element.getAttribute("street"),
                        Integer.parseInt(element.getAttribute("house")),
                        Integer.parseInt(element.getAttribute("floor"))
                );
                addresses.add(address);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return addresses;
    }

    private static void displayStatistics(List<Address> addresses, long duration) {
        Map<Address, Integer> duplicateCount = new HashMap<>();
        Map<String, Map<Integer, Integer>> cityFloorCount = new HashMap<>();

        for (Address address : addresses) {
            duplicateCount.put(address, duplicateCount.getOrDefault(address, 0) + 1);
            cityFloorCount.computeIfAbsent(address.city, k -> new HashMap<>())
                    .put(address.floor, cityFloorCount.get(address.city).getOrDefault(address.floor, 0) + 1);
        }

        System.out.println("Dublicate entries:");
        for (Map.Entry<Address, Integer> entry : duplicateCount.entrySet()) {
            if (entry.getValue() > 1) {
                System.out.println(entry.getKey() + " - " + entry.getValue() + " times");
            }
        }

        System.out.println("\nNumber of buildings in cities:");
        for (Map.Entry<String, Map<Integer, Integer>> entry : cityFloorCount.entrySet()) {
            System.out.println(entry.getKey() + ":");
            for (Map.Entry<Integer, Integer> floorEntry : entry.getValue().entrySet()) {
                System.out.println("  " + floorEntry.getKey() + " storey buildings: " + floorEntry.getValue());
            }
        }

        System.out.println("\nFile processing time: " + duration + " ms");
    }

    static class Address {
        String city;
        String street;
        int house;
        int floor;

        Address(String city, String street, int house, int floor) {
            this.city = city;
            this.street = street;
            this.house = house;
            this.floor = floor;
        }

        @Override
        public String toString() {
            return "City: " + city + ", street: " + street + ", building: " + house + ", floor: " + floor;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Address address = (Address) o;
            return house == address.house &&
                    floor == address.floor &&
                    Objects.equals(city, address.city) &&
                    Objects.equals(street, address.street);
        }

        @Override
        public int hashCode() {
            return Objects.hash(city, street, house, floor);
        }
    }
}
