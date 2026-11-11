package com.broadcasttech.tools;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.View;
import android.widget.*;
import android.text.Editable;
import android.text.TextWatcher;

public class MainActivity extends Activity {
    private final CalculatorState state = new CalculatorState();
    private EditText a, b;
    private TextView result, hint;
    private boolean loading;
    private final String[] en = {"Pi attenuator", "T attenuator", "dBm → watts", "Watts → dBm", "VSWR → return loss", "Return loss → VSWR", "Wavelength in medium"};
    private final String[] es = {"Atenuador Pi", "Atenuador T", "dBm → vatios", "Vatios → dBm", "ROE → pérdida de retorno", "Pérdida de retorno → ROE", "Longitud de onda en medio"};
    private final int blue = Color.rgb(65,155,255), white = Color.rgb(235,241,251);

    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        state.spanish = getPreferences(0).getBoolean("es", false);
        if (saved != null) state.restore(saved.getInt("tool"), saved.getBoolean("es"),
                saved.getStringArray("first"), saved.getStringArray("second"), saved.getBooleanArray("done"));
        draw();
    }
    @Override protected void onSaveInstanceState(Bundle out) {
        out.putInt("tool", state.selected); out.putBoolean("es", state.spanish);
        out.putStringArray("first", state.first.clone()); out.putStringArray("second", state.second.clone());
        out.putBooleanArray("done", state.calculated.clone());
        super.onSaveInstanceState(out);
    }
    private String tr(String en, String es) { return Calculator.text(state.spanish, en, es); }
    private TextView label(String s, int size) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(white); t.setPadding(0,12,0,10); return t;
    }
    private EditText field() {
        EditText e = new EditText(this); e.setSingleLine(true); e.setTextColor(white); e.setHintTextColor(0xff999999);
        e.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL | android.text.InputType.TYPE_NUMBER_FLAG_SIGNED);
        return e;
    }
    private void draw() {
        loading = true;
        ScrollView scroll = new ScrollView(this); scroll.setBackgroundColor(0xff101722);
        LinearLayout layout = new LinearLayout(this); layout.setPadding(34,40,34,40); layout.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(layout); setContentView(scroll);
        // Keep controls clear of system bars with Android 15+ edge-to-edge rendering.
        scroll.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets;
        });
        scroll.requestApplyInsets();
        TextView title = label("Broadcast Tech Tools 1.0",26); title.setTextColor(blue); layout.addView(title);
        Button lang = new Button(this); lang.setText(tr("Language: English ⇄", "Idioma: Español ⇄")); layout.addView(lang);
        lang.setOnClickListener(v -> { state.spanish = !state.spanish; getPreferences(0).edit().putBoolean("es",state.spanish).apply(); draw(); });
        layout.addView(label(tr("Calculator", "Calculadora"),19));
        Spinner mode = new Spinner(this);
        mode.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,state.spanish ? es : en));
        mode.setSelection(state.selected); layout.addView(mode);
        hint = label("",15); layout.addView(hint);
        a = field(); b = field(); layout.addView(a); layout.addView(b);
        layout.addView(label(tr("Decimal point or comma; no thousands separators.", "Punto o coma decimal; sin separadores de miles."),14));
        Button calculate = new Button(this); calculate.setText(tr("CALCULATE", "CALCULAR")); layout.addView(calculate);
        result = label("",20); result.setTextColor(blue); result.setTextIsSelectable(true); layout.addView(result);
        calculate.setOnClickListener(v -> { state.calculated[state.selected] = true; result.setText(state.result()); });
        showTool();
        TextWatcher watcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int start,int count,int after) {}
            public void onTextChanged(CharSequence s,int start,int before,int count) {
                if (!loading) {
                    state.first[state.selected] = a.getText().toString(); state.second[state.selected] = b.getText().toString();
                    state.calculated[state.selected] = false; result.setText("");
                }
            }
            public void afterTextChanged(Editable e) {}
        };
        a.addTextChangedListener(watcher); b.addTextChangedListener(watcher);
        mode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onNothingSelected(AdapterView<?> p) {}
            public void onItemSelected(AdapterView<?> p,View v,int pos,long id) {
                if (pos != state.selected) { state.selected = pos; showTool(); }
            }
        });
        loading = false;
    }
    private void showTool() {
        loading = true;
        String[] hints = state.spanish
                ? new String[]{"Atenuación (dB, ≥ 0) / Impedancia (Ω, > 0)","Atenuación (dB, ≥ 0) / Impedancia (Ω, > 0)","Potencia (dBm)","Potencia (W, > 0)","ROE (≥ 1)","Pérdida de retorno (dB, ≥ 0)","Frecuencia (MHz, > 0) / Factor de velocidad (0 < VF ≤ 1)"}
                : new String[]{"Attenuation (dB, ≥ 0) / Impedance (Ω, > 0)","Attenuation (dB, ≥ 0) / Impedance (Ω, > 0)","Power (dBm)","Power (W, > 0)","VSWR (≥ 1)","Return loss (dB, ≥ 0)","Frequency (MHz, > 0) / Velocity factor (0 < VF ≤ 1)"};
        int i = state.selected; hint.setText(hints[i]);
        a.setContentDescription(hints[i].split(" / ")[0]);
        b.setContentDescription(i == 6 ? tr("Velocity factor", "Factor de velocidad") : tr("Impedance in ohms", "Impedancia en ohmios"));
        b.setVisibility(i == 0 || i == 1 || i == 6 ? View.VISIBLE : View.GONE);
        a.setText(state.first[i]); b.setText(state.second[i]); result.setText(state.result()); loading = false;
    }
}
