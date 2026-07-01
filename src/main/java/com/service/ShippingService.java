package com.service;

import com.entity.Product;
import com.repository.ProductRepository;
import com.request.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Strategy Pattern: Interface định nghĩa chiến lược tính phí ship
 */
interface ShippingStrategy {
    /**
     * Tính phí cơ bản dựa trên trọng lượng
     * 
     * @param weight Trọng lượng đơn hàng (kg)
     * @return Phí cơ bản (VND)
     */
    int calculateBaseFee(double weight);

    /**
     * Lấy ngưỡng trọng lượng miễn phí (kg)
     * 
     * @return Ngưỡng trọng lượng miễn phí
     */
    double getFreeWeightThreshold();

    /**
     * Lấy phí cơ bản cho ngưỡng miễn phí
     * 
     * @return Phí cơ bản (VND)
     */
    int getBaseFee();
}

/**
 * Strategy Pattern: Chiến lược tính phí cho Hà Nội và Hồ Chí Minh
 */
class HanoiHcmcShippingStrategy implements ShippingStrategy {
    private static final double FREE_WEIGHT_THRESHOLD = 3.0;
    private static final int BASE_FEE = 22000;

    @Override
    public int calculateBaseFee(double weight) {
        int fee = BASE_FEE;
        if (weight > FREE_WEIGHT_THRESHOLD) {
            double additionalWeight = weight - FREE_WEIGHT_THRESHOLD;
            int extraUnits = (int) Math.ceil(additionalWeight / 0.5);
            int extraFee = extraUnits * 2500;
            fee += extraFee;
        }
        return fee;
    }

    @Override
    public double getFreeWeightThreshold() {
        return FREE_WEIGHT_THRESHOLD;
    }

    @Override
    public int getBaseFee() {
        return BASE_FEE;
    }
}

/**
 * Strategy Pattern: Chiến lược tính phí cho các khu vực khác
 */
class OtherRegionShippingStrategy implements ShippingStrategy {
    private static final double FREE_WEIGHT_THRESHOLD = 0.5;
    private static final int BASE_FEE = 30000;

    @Override
    public int calculateBaseFee(double weight) {
        int fee = BASE_FEE;
        if (weight > FREE_WEIGHT_THRESHOLD) {
            double additionalWeight = weight - FREE_WEIGHT_THRESHOLD;
            int extraUnits = (int) Math.ceil(additionalWeight / 0.5);
            int extraFee = extraUnits * 2500;
            fee += extraFee;
        }
        return fee;
    }

    @Override
    public double getFreeWeightThreshold() {
        return FREE_WEIGHT_THRESHOLD;
    }

    @Override
    public int getBaseFee() {
        return BASE_FEE;
    }
}

@Service
public class ShippingService {
    @Autowired
    private ProductRepository productRepository;

    // Hà Nội và Hồ Chí Minh: 22,000đ cho 3kg đầu
    private static final List<String> hanoiHcmc = Arrays.asList("Hà Nội", "Hồ Chí Minh");

    // Strategy Pattern: Factory method để chọn strategy phù hợp
    private ShippingStrategy getShippingStrategy(String province) {
        if (hanoiHcmc.contains(province)) {
            return new HanoiHcmcShippingStrategy();
        } else {
            return new OtherRegionShippingStrategy();
        }
    }

    /**
     * Tính phí giao hàng theo đề bài AIMS-ProblemStatement-ver3.0:
     * - HN/HCM: 22,000đ cho 3kg đầu tiên
     * - Các nơi khác: 30,000đ cho 0.5kg đầu tiên
     * - Phí bổ sung: 2,500đ cho mỗi 0.5kg tiếp theo (làm tròn lên)
     * - Free shipping tối đa 25,000đ cho đơn hàng > 100,000đ
     * 
     * Strategy Pattern: Sử dụng strategy để tính phí
     */
    public Integer calculateShippingFee(String shippingAddress, double weight, double orderValue) {
        if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
            return -1;
        }

        if (weight <= 0) {
            return -1;
        }
        String[] addressParts = shippingAddress.split(", ");
        System.out.println("  [calculateShippingFee] Address Parts: " + Arrays.toString(addressParts));
        String province = addressParts[addressParts.length - 1];

        // Strategy Pattern: Chọn strategy dựa trên tỉnh/thành phố
        ShippingStrategy strategy = getShippingStrategy(province);

        System.out.println("  [calculateShippingFee] Province: " + province + ", Strategy: "
                + strategy.getClass().getSimpleName());

        return calculateFee(weight, strategy, orderValue);
    }

    public Double totalWeightCalculate(Item[] items) {
        double weight = 0;
        for (Item item : items) {
            Optional<Product> product = productRepository.findById(item.getProductId());
            if (product.isPresent()) {
                weight += product.get().getWeight() * item.getQuantity();
            } else
                return null;
        }
        return weight;
    }

    public Double totalValueCalculate(Item[] items) {
        double value = 0;
        for (Item item : items) {
            Optional<Product> product = productRepository.findById(item.getProductId());
            if (product.isPresent()) {
                Product p = product.get();
                double itemValue = p.getCurrentPrice() * item.getQuantity();
                System.out.println("  [totalValueCalculate] ProductId: " + item.getProductId()
                        + ", CurrentPrice: " + p.getCurrentPrice()
                        + ", Quantity: " + item.getQuantity()
                        + ", ItemValue: " + itemValue);
                value += itemValue;
            } else {
                System.out.println("  [totalValueCalculate] ProductId: " + item.getProductId() + " NOT FOUND!");
                return null;
            }
        }
        System.out.println("  [totalValueCalculate] Total Value: " + value);
        return value;
    }

    /**
     * Tính phí giao hàng theo AIMS-ProblemStatement-ver3.0
     * Strategy Pattern: Sử dụng strategy để tính phí cơ bản
     */
    private int calculateFee(double weight, ShippingStrategy strategy, double orderValue) {
        double freeWeightThreshold = strategy.getFreeWeightThreshold();
        int baseFee = strategy.getBaseFee();

        System.out
                .println("  [calculateFee] baseFee=" + baseFee + ", freeWeightThreshold=" + freeWeightThreshold + "kg");

        // Strategy Pattern: Delegate việc tính phí cho strategy
        int fee = strategy.calculateBaseFee(weight);

        // Log chi tiết nếu có phí bổ sung
        if (weight > freeWeightThreshold) {
            double additionalWeight = weight - freeWeightThreshold;
            int extraUnits = (int) Math.ceil(additionalWeight / 0.5);
            int extraFee = extraUnits * 2500;
            System.out.println("  [calculateFee] additionalWeight=" + additionalWeight + "kg, extraUnits=" + extraUnits
                    + ", extraFee=" + extraFee);
        }

        System.out.println("  [calculateFee] Fee before discount: " + fee);

        // Free shipping tối đa 25,000đ cho đơn hàng > 100,000đ
        if (orderValue > 100000) {
            int discount = Math.min(fee, 25000);
            System.out.println(
                    "  [calculateFee] DISCOUNT APPLIED! orderValue=" + orderValue + " > 100000, discount=" + discount);
            fee = fee - discount;
        } else {
            System.out.println("  [calculateFee] NO DISCOUNT - orderValue=" + orderValue + " <= 100000");
        }

        System.out.println("  [calculateFee] Final fee: " + fee);
        return fee;
    }
}
