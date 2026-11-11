package com.broadcasttech.tools;
import java.util.Locale;

/** Seven offline calculators. Decimal separators are accepted; grouping is not. */
public final class Calculator {
    private Calculator() {}
    public static String text(boolean es, String en, String sp) { return es ? sp : en; }
    private static IllegalArgumentException error(boolean es, String en, String sp) {
        return new IllegalArgumentException(text(es, en, sp));
    }
    private static IllegalArgumentException range(boolean es) {
        return error(es, "Value or result is outside the supported numeric range.",
                "El valor o resultado está fuera del intervalo numérico admitido.");
    }
    public static double number(String raw, boolean es) {
        String s = raw == null ? "" : raw.trim();
        if (!s.matches("[+-]?(?:[0-9]+(?:[.,][0-9]*)?|[.,][0-9]+)(?:[eE][+-]?[0-9]+)?"))
            throw error(es, "Enter a number using a decimal point or comma, without thousands separators.",
                    "Introduce un número con punto o coma decimal, sin separadores de miles.");
        double n;
        try { n = Double.parseDouble(s.replace(',', '.')); }
        catch (NumberFormatException e) { throw range(es); }
        if (!Double.isFinite(n)) throw range(es);
        if (n == 0 && s.split("[eE]", 2)[0].matches(".*[1-9].*")) throw range(es);
        return n;
    }
    private static double positive(double n, boolean es) {
        if (!Double.isFinite(n) || n <= 0) throw range(es);
        return n;
    }
    private static String value(double n, boolean es) {
        if (n == Double.POSITIVE_INFINITY) return "∞";
        if (!Double.isFinite(n)) throw range(es);
        return String.format(es ? Locale.forLanguageTag("es") : Locale.US, "%.9g", n == 0 ? 0 : n);
    }
    private static String line(String label, double n, String unit, boolean es) {
        return label + value(n, es) + unit;
    }
    public static String calculate(int tool, String first, String second, boolean es) {
        try {
            double x = number(first, es);
            double z = tool == 0 || tool == 1 || tool == 6 ? number(second, es) : 0;
            switch (tool) {
                case 0: case 1: {
                    if (x < 0 || z <= 0) throw error(es,
                            "Attenuation must be nonnegative and impedance positive.",
                            "La atenuación debe ser mayor o igual que cero y la impedancia positiva.");
                    double series, shunt;
                    if (x == 0) { series = 0; shunt = Double.POSITIVE_INFINITY; }
                    else {
                        double t = x * (Math.log(10) / 20);
                        double half = Math.tanh(t / 2);
                        // Stable at small attenuation and without squaring a huge voltage ratio.
                        double logSinh = t < 20 ? Math.log(Math.sinh(t))
                                : t - Math.log(2) + Math.log1p(-Math.exp(-2 * t));
                        if (tool == 0) {
                            shunt = positive(z / half, es);
                            series = positive(Math.exp(Math.log(z) + logSinh), es);
                        } else {
                            series = positive(z * half, es);
                            shunt = positive(Math.exp(Math.log(z) - logSinh), es);
                        }
                    }
                    String s = text(es, "Series R", "R en serie ");
                    String p = text(es, "Shunt R", "R en paralelo ");
                    String result = tool == 0
                            ? line(p + "1 = ", shunt, " Ω", es) + "\n" + line(s + "2 = ", series, " Ω", es) + "\n" + line(p + "3 = ", shunt, " Ω", es)
                            : line(s + "1 = ", series, " Ω", es) + "\n" + line(p + "2 = ", shunt, " Ω", es) + "\n" + line(s + "3 = ", series, " Ω", es);
                    return result + (x == 0 ? text(es, "\n0 Ω = short; ∞ Ω = open circuit.", "\n0 Ω = cortocircuito; ∞ Ω = circuito abierto.") : "");
                }
                case 2: return line("", positive(Math.pow(10, x / 10 - 3), es), " W", es);
                case 3:
                    if (x <= 0) throw error(es, "Power must be positive.", "La potencia debe ser positiva.");
                    return line("", 10 * Math.log10(x) + 30, " dBm", es);
                case 4: {
                    if (x < 1) throw error(es, "VSWR must be at least 1.", "La ROE debe ser mayor o igual que 1.");
                    double rho = (x - 1) / x / (1 + 1 / x);
                    double loss = x == 1 ? Double.POSITIVE_INFINITY
                            : (x > 2 ? 20 / Math.log(10) * Math.log1p(2 / (x - 1)) : -20 * Math.log10(rho));
                    return line(text(es, "Return loss: ", "Pérdida de retorno: "), loss, " dB", es)
                            + "\n" + line(text(es, "Reflection coefficient: ", "Coeficiente de reflexión: "), rho, "", es);
                }
                case 5: {
                    if (x < 0) throw error(es, "Return loss must be nonnegative.", "La pérdida de retorno debe ser mayor o igual que cero.");
                    double t = x * (Math.log(10) / 20);
                    double rho = x == 0 ? 1 : positive(Math.exp(-t), es);
                    double vswr = x == 0 ? Double.POSITIVE_INFINITY : positive((1 + rho) / -Math.expm1(-t), es);
                    return line(text(es, "VSWR: ", "ROE: "), vswr, "", es)
                            + "\n" + line(text(es, "Reflection coefficient: ", "Coeficiente de reflexión: "), rho, "", es)
                            + (x == 0 ? text(es, "\nTotal reflection.", "\nReflexión total.") : "");
                }
                case 6:
                    if (x <= 0 || z <= 0 || z > 1) throw error(es,
                            "Frequency must be positive; velocity factor must be greater than 0 and at most 1.",
                            "La frecuencia debe ser positiva; el factor de velocidad debe ser mayor que 0 y como máximo 1.");
                    return line(text(es, "Wavelength: ", "Longitud de onda: "), positive(299.792458 * z / x, es), " m", es);
                default: throw error(es, "Unknown calculator.", "Calculadora desconocida.");
            }
        } catch (IllegalArgumentException e) {
            return text(es, "Invalid input: ", "Entrada no válida: ") + e.getMessage();
        }
    }
}
