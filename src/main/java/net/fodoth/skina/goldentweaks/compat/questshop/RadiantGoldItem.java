package net.fodoth.skina.goldentweaks.compat.questshop;

public class RadiantGoldItem extends AbstractGoldCoinItem {

    public RadiantGoldItem(Properties properties) {
        super(properties);
    }

    @Override
    protected int multiplier() {
        return 9;
    }
}