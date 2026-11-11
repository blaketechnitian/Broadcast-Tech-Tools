package com.broadcasttech.tools;

import android.app.*;
import android.os.*;
import android.graphics.Color;
import android.content.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.text.*;

public class MainActivity extends Activity {
 boolean spanish=false;
 LinearLayout layout;
 Spinner mode;
 EditText a,b;
 TextView result, hint;
 final String[] en={"Pi attenuator","T attenuator","dBm → watts","Watts → dBm","VSWR → return loss","Return loss → VSWR","Wavelength in medium"};
 final String[] es={"Atenuador Pi","Atenuador T","dBm → vatios","Vatios → dBm","ROE → pérdida de retorno","Pérdida de retorno → ROE","Longitud de onda en medio"};
 int blue=Color.rgb(65,155,255),white=Color.rgb(235,241,251);
 public void onCreate(Bundle b){super.onCreate(b);spanish=getPreferences(0).getBoolean("es",false); draw();}
 TextView label(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(white);t.setPadding(0,12,0,10);return t;}
 EditText field(String value){EditText e=new EditText(this);e.setSingleLine(true);e.setText(value);e.setTextColor(white);e.setHintTextColor(0xff999999);e.setInputType( android.text.InputType.TYPE_CLASS_NUMBER|android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL|android.text.InputType.TYPE_NUMBER_FLAG_SIGNED);return e;}
 void draw(){
  ScrollView scroll=new ScrollView(this);scroll.setBackgroundColor(0xff101722);
  layout=new LinearLayout(this);layout.setPadding(34,40,34,40);layout.setOrientation(1);scroll.addView(layout);setContentView(scroll);
  TextView title=label("Broadcast Tech Tools",26);title.setTextColor(blue);layout.addView(title);
  Button lang=new Button(this);lang.setText(spanish?"Language: Español  ⇄":"Language: English  ⇄");layout.addView(lang);
  lang.setOnClickListener(v->{spanish=!spanish;getPreferences(0).edit().putBoolean("es",spanish).apply();draw();});
  layout.addView(label(spanish?"Calculadora":"Calculator",19));
  mode=new Spinner(this);String[] options=spanish?es:en;
  ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,options);mode.setAdapter(adapter);layout.addView(mode);
  hint=label("",15);layout.addView(hint);
  a=field("6");b=field("50");
  layout.addView(a);layout.addView(b);
  Button calculate=new Button(this);calculate.setText(spanish?"CALCULAR":"CALCULATE");layout.addView(calculate);
  result=label("",20);result.setTextColor(blue);layout.addView(result);
  calculate.setOnClickListener(v->calculate());
  mode.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){
   public void onNothingSelected(android.widget.AdapterView<?> p){}
   public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){
    String[] h=spanish?new String[]{"Atenuación (dB) / Impedancia (Ω)","Atenuación (dB) / Impedancia (Ω)","Potencia (dBm)","Potencia (W)","ROE (> 1)","Pérdida de retorno (dB, > 0)","Frecuencia (MHz) / Factor de velocidad (0–1)"}:
     new String[]{"Attenuation (dB) / Impedance (Ω)","Attenuation (dB) / Impedance (Ω)","Power (dBm)","Power (W)","VSWR (> 1)","Return loss (dB, > 0)","Frequency (MHz) / Velocity factor (0–1)"};
    hint.setText(h[pos]);b.setVisibility((pos==0||pos==1||pos==6)?View.VISIBLE:View.GONE);a.setText(pos==4?"1.5":pos==5?"20":pos==6?"100":"6");b.setText(pos==6?"0.66":"50");result.setText("");
   }
  });
 }
 void calculate(){
  try{
   double x=Double.parseDouble(a.getText().toString()),z=Double.parseDouble(b.getText().toString()), k,sh,ser,ans;int i=mode.getSelectedItemPosition();
   String s="";
   switch(i){
    case 0: case 1:
     if(x<=0||z<=0)throw new Exception("Attenuation and impedance must be positive.");
     k=Math.pow(10,x/20.0);
     if(i==0){sh=z*(k+1)/(k-1);ser=z*(k*k-1)/(2*k);s=fmt("Shunt R1 = %.4f Ω\nSeries R2 = %.4f Ω\nShunt R3 = %.4f Ω",sh,ser,sh);}
     else {ser=z*(k-1)/(k+1);sh=2*z*k/(k*k-1);s=fmt("Series R1 = %.4f Ω\nShunt R2 = %.4f Ω\nSeries R3 = %.4f Ω",ser,sh,ser);}break;
    case 2:ans=Math.pow(10,(x-30)/10);s=fmt("%.9g W",ans);break;
    case 3:if(x<=0)throw new Exception("Power must be positive.");s=fmt("%.4f dBm",10*Math.log10(x*1000));break;
    case 4:if(x<=1)throw new Exception("VSWR must be greater than 1.");ans=(x-1)/(x+1);s=fmt("Return loss: %.4f dB\nReflection coefficient: %.6f",-20*Math.log10(ans),ans);break;
    case 5:if(x<=0)throw new Exception("Return loss must be positive.");ans=Math.pow(10,-x/20);s=fmt("VSWR: %.5f\nReflection coefficient: %.6f",(1+ans)/(1-ans),ans);break;
    case 6:if(x<=0||z<=0||z>1)throw new Exception("Frequency > 0, velocity factor in (0,1].");s=fmt("Wavelength: %.6f m",299.792458*z/x);break;
   }
   result.setText(s);
  }catch(Exception e){result.setText((spanish?"Entrada no válida: ":"Invalid input: ")+e.getMessage());}
 }
 String fmt(String f,Object... args){return String.format(Locale.US,f,args);}
}