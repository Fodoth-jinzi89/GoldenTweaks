package net.fodoth.skina.goldentweaks.compat.questshop;

public class LuminousGoldItem extends AbstractGoldCoinItem {

    public LuminousGoldItem(Properties properties) {
        super(properties);
    }

    @Override
    protected int multiplier() {
        return 6561;
    }
}