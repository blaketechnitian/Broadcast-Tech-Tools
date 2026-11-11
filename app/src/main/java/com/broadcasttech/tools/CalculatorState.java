package com.broadcasttech.tools;

/** Per-tool drafts survive language and tool changes; Activity serializes this state. */
public final class CalculatorState {
    public static final int COUNT = 7;
    public int selected;
    public boolean spanish;
    public final String[] first = {"6", "6", "0", "1", "1.5", "20", "100"};
    public final String[] second = {"50", "50", "", "", "", "", "0.66"};
    public final boolean[] calculated = new boolean[COUNT];
    public String result() {
        return calculated[selected] ? Calculator.calculate(selected, first[selected], second[selected], spanish) : "";
    }
    public void restore(int tool, boolean es, String[] a, String[] b, boolean[] done) {
        selected = tool >= 0 && tool < COUNT ? tool : 0;
        spanish = es;
        if (a != null && b != null && done != null && a.length == COUNT && b.length == COUNT && done.length == COUNT) {
            for (int i = 0; i < COUNT; i++) {
                first[i] = a[i] == null ? "" : a[i];
                second[i] = b[i] == null ? "" : b[i];
                calculated[i] = done[i];
            }
        }
    }
}
