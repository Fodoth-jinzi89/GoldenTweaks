package net.fodoth.skina.goldentweaks.compat.questshop;

public class GloriousGoldItem extends AbstractGoldCoinItem {

    public GloriousGoldItem(Properties properties) {
        super(properties);
    }

    @Override
    protected int multiplier() {
        return 729;
    }
}