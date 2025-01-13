package com.example.migator;

import java.util.List;

public class Trajectory {
    private String type;
    private Attributes attributes;
    private List<Feature> features;

    public String getType() {
        return type;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    // Klasa dla pola "attributes"
    public static class Attributes {
        private int line_id;
        private String line_number;
        private String line_type;
        private String line_subtype;
        private String vehicle_type;
        private String updated_at;

        public int getLineId() {
            return line_id;
        }

        public String getLineNumber() {
            return line_number;
        }

        public String getLineType() {
            return line_type;
        }

        public String getLineSubtype() {
            return line_subtype;
        }

        public String getVehicleType() {
            return vehicle_type;
        }

        public String getUpdatedAt() {
            return updated_at;
        }
    }

    // Klasa dla elementów listy "features"
    public static class Feature {
        private String type;
        private Properties properties;
        private Geometry geometry;

        public String getType() {
            return type;
        }

        public Properties getProperties() {
            return properties;
        }

        public Geometry getGeometry() {
            return geometry;
        }

        // Klasa dla pola "properties"
        public static class Properties {
            private int route_variant_number;
            private String route_variant_type;

            public int getRouteVariantNumber() {
                return route_variant_number;
            }

            public String getRouteVariantType() {
                return route_variant_type;
            }
        }

        // Klasa dla pola "geometry"
        public static class Geometry {
            private String type;
            private List<List<Double>> coordinates;

            public String getType() {
                return type;
            }

            public List<List<Double>> getCoordinates() {
                return coordinates;
            }
        }
    }
}
