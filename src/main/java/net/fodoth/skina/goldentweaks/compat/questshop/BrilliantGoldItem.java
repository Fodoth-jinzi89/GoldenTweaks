package net.fodoth.skina.goldentweaks.compat.questshop;

public class BrilliantGoldItem extends AbstractGoldCoinItem {

    public BrilliantGoldItem(Properties properties) {
        super(properties);
    }

    @Override
    protected int multiplier() {
        return 81;
    }
}