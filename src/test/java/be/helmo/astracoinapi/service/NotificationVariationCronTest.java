package be.helmo.astracoinapi.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotificationVariationCronTest {

    @Test
    public void testBasicVariation(){
        assertEquals(648,NotificationVariationCron.variation(250,1870));
        assertEquals(-53.25,NotificationVariationCron.variation(4000,1870));
        assertEquals(0,NotificationVariationCron.variation(0,0));
        assertEquals(-100,NotificationVariationCron.variation(20,0));
        assertEquals(Double.POSITIVE_INFINITY,NotificationVariationCron.variation(0,20));
    }

    @Test
    public void testIsOnVariation(){
        assertTrue(NotificationVariationCron.isOnVariation(250,1870, 1,-1));
        assertFalse(NotificationVariationCron.isOnVariation(250,251, 1,-1));
        assertFalse(NotificationVariationCron.isOnVariation(250,252, 1,-1));
        assertTrue(NotificationVariationCron.isOnVariation(250,253, 1,-1));

        assertTrue(NotificationVariationCron.isOnVariation(253,250, 1,-1));
        assertFalse(NotificationVariationCron.isOnVariation(252,253, 1,-1));
        assertFalse(NotificationVariationCron.isOnVariation(251,250, 1,-1));
        assertTrue(NotificationVariationCron.isOnVariation(1870,250, 1,-1));
    }

}