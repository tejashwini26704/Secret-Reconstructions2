import java.io.*;
import java.math.BigInteger;
import java.util.*;

public class SecretReconstruction {

    public static void main(String[] args) throws Exception {

        // Read the JSON file manually
        BufferedReader br = new BufferedReader(new FileReader("input.json"));
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            jsonBuilder.append(line.trim());
        }
        br.close();

        String json = jsonBuilder.toString();

        // Extract n and k
        int n = Integer.parseInt(json.split("\"n\"\\s*:\\s*")[1].split(",")[0].trim());
        int k = Integer.parseInt(json.split("\"k\"\\s*:\\s*")[1].split("}")[0].trim());

        // Extract all shares
        Map<Integer, BigInteger> shares = new LinkedHashMap<>();
        String[] parts = json.split("},");
        for (String part : parts) {
            if (part.contains("\"keys\"")) continue;
            if (!part.contains("base")) continue;

            String keyStr = part.split(":\\s*\\{")[0].replaceAll("[^0-9]", "").trim();
            if (keyStr.isEmpty()) continue;

            int x = Integer.parseInt(keyStr);
            String baseStr = part.split("\"base\"\\s*:\\s*\"")[1].split("\"")[0];
            String valueStr = part.split("\"value\"\\s*:\\s*\"")[1].split("\"")[0];

            int base = Integer.parseInt(baseStr);
            BigInteger y = new BigInteger(valueStr, base);
            shares.put(x, y);
        }

        // Convert shares into a list
        List<int[]> points = new ArrayList<>();
        for (Map.Entry<Integer, BigInteger> entry : shares.entrySet()) {
            points.add(new int[]{entry.getKey(), entry.getValue().intValue()});
        }

        // Detect incorrect share and reconstruct secret
        int wrongX = -1;
        int secret = -1;

        for (int i = 0; i < points.size(); i++) {
            List<int[]> subset = new ArrayList<>(points);
            subset.remove(i);

            int possibleSecret = lagrangeInterpolation(subset, k);

            boolean valid = true;
            for (int j = 0; j < subset.size(); j++) {
                if (evaluatePolynomial(subset, subset.get(j)[0]) != subset.get(j)[1]) {
                    valid = false;
                    break;
                }
            }

            if (valid) {
                wrongX = points.get(i)[0];
                secret = possibleSecret;
                break;
            }
        }

        // Output results
        System.out.println("Reconstructed Secret: " + secret);
        System.out.println("Incorrect Share: " + wrongX);
    }

    // Lagrange Interpolation Formula
    private static int lagrangeInterpolation(List<int[]> points, int k) {
        double secret = 0;
        for (int i = 0; i < k; i++) {
            double term = points.get(i)[1];
            for (int j = 0; j < k; j++) {
                if (j != i) {
                    term *= (0 - points.get(j)[0]) * 1.0 / (points.get(i)[0] - points.get(j)[0]);
                }
            }
            secret += term;
        }
        return (int) Math.round(secret);
    }

    private static int evaluatePolynomial(List<int[]> points, int x) {
        double result = 0;
        int k = points.size();
        for (int i = 0; i < k; i++) {
            double term = points.get(i)[1];
            for (int j = 0; j < k; j++) {
                if (j != i) {
                    term *= (x - points.get(j)[0]) * 1.0 / (points.get(i)[0] - points.get(j)[0]);
                }
            }
            result += term;
        }
        return (int) Math.round(result);
    }
}
