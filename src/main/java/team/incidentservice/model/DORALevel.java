package team.incidentservice.model;

public enum DORALevel
    {
        UNKNOWN,
        LOW,
        MEDIUM,
        HIGH,
        ELITE;
    
        public static final long MONTH = 30 * 24 * 60 * 60;
        public static final long WEEK = 7 * 24 * 60 * 60;
        public static final long DAY = 24 * 60 * 60;
        public static final long HOUR = 60 * 60;
    }
