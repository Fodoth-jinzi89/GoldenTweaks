package net.fodoth.skina.goldentweaks.compat.questshop;

public class ImmaculateGoldItem extends AbstractGoldCoinItem {

    public ImmaculateGoldItem(Properties properties) {
        super(properties);
    }

    @Override
    protected int multiplier() {
        return 59049;
    }
}