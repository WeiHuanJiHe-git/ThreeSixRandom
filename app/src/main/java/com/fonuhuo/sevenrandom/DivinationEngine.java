package com.fonuhuo.sevenrandom;

import java.util.HashMap;
import java.util.Map;

public final class DivinationEngine {
    private DivinationEngine() {}

    private static final class PalaceInfo {
        final String element;
        final String symbol;
        final String verse;
        final String before;
        final String during;
        final String after;
        final int score;

        PalaceInfo(String element, String symbol, String verse,
                   String before, String during, String after, int score) {
            this.element = element;
            this.symbol = symbol;
            this.verse = verse;
            this.before = before;
            this.during = during;
            this.after = after;
            this.score = score;
        }
    }

    private static final Map<String, PalaceInfo> DATA = new HashMap<>();

    static {
        DATA.put("大安", new PalaceInfo("木", "青龙",
                "大安事事昌，求谋在神方，失物去不远，宅舍保安康；行人身未动，病者主无妨，将军回田野，仔细与推详。",
                "起势较稳，基础条件相对完整，宜先守住已有优势。",
                "过程偏稳，不宜为求快而频繁改动。",
                "结果倾向稳定落地，重在守成与按计划推进。", 2));
        DATA.put("留连", new PalaceInfo("水", "玄武",
                "留连事难成，求谋日示明，官事只宜缓，去者未回程；失物南方见，急讨方称心，更须防口舌，人口且平平。",
                "前期存在拖延、牵扯或信息不清，事情容易卡在准备阶段。",
                "过程反复，适合补资料、等条件、拆阻塞，不适合硬推。",
                "结果往往不是立刻定局，需要延后或经历一轮调整。", -1));
        DATA.put("速喜", new PalaceInfo("火", "朱雀",
                "速喜喜来临，求财向南行，失物申未午，逢人路上寻；官事有福德，病者无祸侵，田定六畜吉，行人有信音。",
                "起势快，通常已有明显机会或消息正在形成。",
                "过程推进速度较快，适合抓窗口、及时行动。",
                "结果多表现为较快反馈、消息落地或短期见效。", 2));
        DATA.put("赤口", new PalaceInfo("金", "白虎",
                "赤日主口舌，官非切要防，失物急去寻，行人有惊慌；鸡犬多作怪，病者出西方，更须访咒诅，恐怕染瘟癀。",
                "起点存在冲突、争议、误解或立场不一致。",
                "过程中最需要控制沟通成本，避免情绪化决策和正面硬碰。",
                "结果容易留下争执、损耗或关系成本，宜先化解冲突再求结果。", -2));
        DATA.put("小吉", new PalaceInfo("木", "六合",
                "小吉最吉昌，路上好商量，阴人来报喜，失物在坤方；行人立便至，交关甚是强，凡事皆和合，病者祷上苍。",
                "起点有人和、协作或可谈空间，条件虽未必强势但较顺。",
                "过程适合协商、合作、借力，往往靠配合而不是强攻。",
                "结果倾向和合、达成共识或得到可接受的收益。", 2));
        DATA.put("空亡", new PalaceInfo("土", "勾陈",
                "空亡事不长，阴人小乖张，求财无利益，行人有灾殃；失物寻不见，官事有刑场，病人逢暗鬼，禳解保安康。",
                "起点可能存在目标虚、信息缺、预期过高或条件尚未成形。",
                "过程容易出现落空、失焦或投入没有及时反馈，应先验证前提。",
                "结果有落空或重新定义目标的可能，适合止损、复盘、重开一局。", -2));
    }

    public static String interpret(String[] palaces) {
        if (palaces == null || palaces.length != 3) {
            throw new IllegalArgumentException("Exactly three palaces are required");
        }
        PalaceInfo a = info(palaces[0]);
        PalaceInfo b = info(palaces[1]);
        PalaceInfo c = info(palaces[2]);

        String trend = trend(a.score, b.score, c.score);
        String relation = relation(palaces, a.score, b.score, c.score);

        return "【三阶段解读】\n"
                + "前态 · " + palaces[0] + "：" + a.before + "\n\n"
                + "事中 · " + palaces[1] + "：" + b.during + "\n\n"
                + "结果 · " + palaces[2] + "：" + c.after + "\n\n"
                + "【综合】\n" + trend + " " + relation + "\n\n"
                + "【最终宫原文】\n"
                + palaces[2] + " · 属" + c.element + " · " + c.symbol + "\n" + c.verse
                + "\n\n注：原文据《玉匣记·杂占篇·李淳风六壬时课》通行本整理；三阶段解释为本应用的现代化推演，不等同于古籍原文。";
    }

    public static String originalText(String palace) {
        PalaceInfo p = info(palace);
        return palace + " · 属" + p.element + " · " + p.symbol + "\n" + p.verse;
    }

    private static PalaceInfo info(String palace) {
        PalaceInfo p = DATA.get(palace);
        if (p == null) throw new IllegalArgumentException("Unknown palace: " + palace);
        return p;
    }

    private static String trend(int a, int b, int c) {
        if (a <= 0 && b <= 0 && c > 0) return "整体是先难后易，后段明显转好。";
        if (a > 0 && b <= 0 && c > 0) return "起点不错，中途受阻，但最终仍有回正空间。";
        if (a > 0 && b > 0 && c <= 0) return "前中段顺，但后段转弱，最需要防止高开低走。";
        if (a <= 0 && b > 0 && c > 0) return "由弱转强，关键转折出现在事情进行阶段。";
        if (a > 0 && b > 0 && c > 0) return "三段整体偏顺，重点是不要因为顺势而过度冒进。";
        if (a <= 0 && b <= 0 && c <= 0) return "三段阻力都偏大，优先验证前提、控制投入和预留退出空间。";
        return "走势有明显波动，不宜只看单一宫位，应把过程变化和最终落宫合并判断。";
    }

    private static String relation(String[] p, int a, int b, int c) {
        if (p[0].equals(p[1]) && p[1].equals(p[2])) {
            return "三宫同象，主题非常集中，事情容易沿同一种状态持续发展。";
        }
        if (p[0].equals(p[2])) {
            return "首尾同宫，说明中途虽有变化，最后仍可能回到最初的核心状态。";
        }
        if (c > b && b >= a) {
            return "状态逐段改善，后续比前期更值得关注。";
        }
        if (c < b && b <= a) {
            return "状态逐段走弱，越到后面越需要收敛风险。";
        }
        return "中间宫是主要变量，决定事情如何从起点走向最终结果。";
    }
}
