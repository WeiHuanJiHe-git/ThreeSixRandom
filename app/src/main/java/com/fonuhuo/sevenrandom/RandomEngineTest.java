package com.fonuhuo.sevenrandom;

import java.util.Random;

public final class RandomEngineTest {
    public static void main(String[] args) {
        RandomEngine engine = new RandomEngine(new Random(1L));
        String[] p = engine.calculatePalaces(new int[]{10, 3, 8});
        require("赤口".equals(p[0]), "10 should land on 赤口");
        require("空亡".equals(p[1]), "then 3 should land on 空亡");
        require("大安".equals(p[2]), "then 8 should land on 大安");

        int[] n = engine.generateThree();
        for (int x : n) require(x >= 1 && x <= 999, "number out of range");

        String[] all = RandomEngine.PALACES;
        int count = 0;
        for (String a : all) for (String b : all) for (String c : all) {
            String s = DivinationEngine.interpret(new String[]{a,b,c});
            require(s.contains(a) && s.contains(b) && s.contains(c), "missing interpretation");
            count++;
        }
        require(count == 216, "not all 216 combinations covered");
        System.out.println("PASS: core rules + 216 interpretations");
    }

    private static void require(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }
}
