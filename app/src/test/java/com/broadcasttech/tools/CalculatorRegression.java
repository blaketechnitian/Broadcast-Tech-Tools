package com.broadcasttech.tools;

import java.util.regex.*;

/** Executed by verifyCalculators and before every APK build; no Android dependency. */
public final class CalculatorRegression {
    private static int checks;
    private static void check(String label, boolean ok) {
        checks++;
        if (!ok) throw new AssertionError(label);
        System.out.println("PASS: " + label);
    }
    private static String calc(int t, String a, String b) { return Calculator.calculate(t,a,b,false); }
    private static double firstNumber(String s) {
        Matcher m = Pattern.compile("[+-]?[0-9]+(?:\\.[0-9]+)?(?:[eE][+-]?[0-9]+)?").matcher(s);
        if (!m.find()) throw new AssertionError("No number: " + s);
        return Double.parseDouble(m.group());
    }
    private static void near(String name, double actual, double expected) {
        check(name, Double.isFinite(actual) && Math.abs(actual-expected) <= Math.max(Math.abs(expected)*1e-8,1e-310));
    }
    private static void invalid(int t, String a, String b) {
        check("reject tool " + t + ": " + a + ", " + b, calc(t,a,b).startsWith("Invalid input: "));
    }
    public static void main(String[] args) {
        near("0 dBm",firstNumber(calc(2,"0","")),0.001);
        near("30 dBm ignores unused field",firstNumber(calc(2,"30","not a number")),1);
        near("negative dBm",firstNumber(calc(2,"-30","")),1e-6);
        near("1 W",firstNumber(calc(3,"1","")),30);
        near("1 mW",firstNumber(calc(3,"0.001","")),0);
        near("large finite watts without intermediate overflow",firstNumber(calc(3,"1e308","")),3110);
        near("smallest positive watts",firstNumber(calc(3,Double.toString(Double.MIN_VALUE),"")),10*Math.log10(Double.MIN_VALUE)+30);
        near("VSWR 3 return loss",firstNumber(calc(4,"3","")),6.020599913279624);
        near("20 dB return loss",firstNumber(calc(5,"20","")),11.0/9);
        near("100 MHz, VF .66",firstNumber(calc(6,"100","0.66")),1.9786302228);
        near("vacuum VF 1",firstNumber(calc(6,"100","1")),2.99792458);
        near("comma decimal",firstNumber(calc(6,"100","0,66")),1.9786302228);
        near("trimmed signed decimal",Calculator.number(" +1,5 ",false),1.5);
        near("decimal exponent",Calculator.number("1,5e2",false),150);
        check("perfect match",calc(4,"1","").contains("∞ dB") && calc(4,"1","").contains("0.00000000"));
        check("total reflection",calc(5,"0","").contains("VSWR: ∞") && calc(5,"0","").contains("Total reflection"));
        check("zero attenuation Pi",calc(0,"0","50").contains("∞ Ω") && calc(0,"0","50").contains("short"));
        check("zero attenuation T",calc(1,"0","50").contains("∞ Ω") && calc(1,"0","50").contains("short"));
        check("small nonzero wavelength is not formatted as zero",calc(6,"1e20","1").contains("e-18"));
        check("huge VSWR gives finite positive loss",firstNumber(calc(4,"1e308",""))>0);
        near("tiny return loss stable denominator",firstNumber(calc(5,"1e-20","")),40/Math.log(10)/1e-20);
        for (String s : new String[]{"", "abc", "NaN", "Infinity", "-Infinity", "1e400", "1e-400", "1,234.56", "1.234,56", "1,2,3"}) invalid(2,s,"");
        invalid(0,"-1","50"); invalid(1,"6","0"); invalid(0,"6","NaN");
        invalid(3,"0",""); invalid(3,"-1",""); invalid(4,"0.99",""); invalid(5,"-1","");
        invalid(6,"0","0.66"); invalid(6,"100","0"); invalid(6,"100","1.01");
        invalid(2,"4000",""); invalid(2,"-4000",""); invalid(0,"1e308","50");
        invalid(1,"1e308","50"); invalid(5,"10000",""); invalid(6,"1e-308","1");
        // Check matched attenuator networks independently through their terminated impedances/gain.
        for (double db : new double[]{0.001,6.020599913279624,20,60}) {
            for(int tool=0;tool<2;tool++) {
                String output=calc(tool,Double.toString(db),"50");
                String[] lines=output.split("\\n");
                double r1=firstNumber(lines[0].split(" = ")[1]);
                double r2=firstNumber(lines[1].split(" = ")[1]);
                double input, gain;
                if(tool==0) {
                    double load=1/(1/r1+1/50.0);
                    input=1/(1/r1+1/(r2+load)); gain=load/(r2+load);
                } else {
                    double parallel=1/(1/r2+1/(r1+50));
                    input=r1+parallel; gain=parallel/(r1+parallel)*50/(r1+50);
                }
                near("matched input tool="+tool+" dB="+db,input,50);
                near("attenuation tool="+tool+" dB="+db,-20*Math.log10(gain),db);
            }
        }
        for(double dbm:new double[]{-200,-30,0,30,200}) {
            double watts=firstNumber(calc(2,Double.toString(dbm),""));
            near("power round trip "+dbm,firstNumber(calc(3,Double.toString(watts),"")),dbm);
        }
        for(double vswr:new double[]{1.01,1.5,3,100}) {
            double rl=firstNumber(calc(4,Double.toString(vswr),""));
            near("VSWR round trip "+vswr,firstNumber(calc(5,Double.toString(rl),"")),vswr);
        }
        check("Spanish output",Calculator.calculate(6,"100","0,66",true).startsWith("Longitud de onda: 1,978"));
        check("Spanish error",Calculator.calculate(3,"0","",true).equals("Entrada no válida: La potencia debe ser positiva."));
        check("Spanish parser error",Calculator.calculate(2,"abc","",true).contains("Introduce un número"));
        check("Spanish resistors",Calculator.calculate(0,"6","50",true).contains("R en paralelo"));
        check("Spanish reflection",Calculator.calculate(5,"20","",true).contains("Coeficiente de reflexión"));
        CalculatorState state=new CalculatorState(); state.selected=6; state.first[6]="101,5"; state.second[6]="0,66"; state.calculated[6]=true;
        String english=state.result(); state.spanish=true;
        check("language keeps selected tool",state.selected==6);
        check("language keeps both drafts",state.first[6].equals("101,5") && state.second[6].equals("0,66"));
        check("language translates computed result",state.result().startsWith("Longitud de onda:"));
        state.spanish=false; check("language round trip result",state.result().equals(english));
        state.selected=2; state.first[2]="30"; state.calculated[2]=true; state.selected=6;
        check("switching tools retains result",state.result().equals(english));
        CalculatorState restored=new CalculatorState();
        restored.restore(state.selected,true,state.first.clone(),state.second.clone(),state.calculated.clone());
        check("restoration retains drafts and result",restored.selected==6 && restored.first[6].equals("101,5") && restored.result().startsWith("Longitud de onda:"));
        restored.selected=2; near("restoration retains other tool",firstNumber(restored.result().replace(',','.')),1);
        restored.restore(100,false,null,null,null); check("invalid restored selection",restored.selected==0);
        check("seven tools only",CalculatorState.COUNT==7);
        System.out.println("ALL "+checks+" CHECKS PASSED");
    }
}
