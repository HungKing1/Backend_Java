package com.controller;

public class PlaceOrderController {
    public boolean validatePhoneNumber(String number) {
        if (number == null || number.isEmpty()) {
            return false;
        }
        // Phone must be 10 digits and start with 0
        return number.matches("^0\\d{9}$");
    }
}
