package net.fodoth.skina.goldentweaks.mixin.fix.extendedae_plus;

/**
 * Retired: this used to {@code @ModifyConstant} extendedae_plus' {@code eap$updateButtonsLayout}. It was replaced by
 * {@link PatternProviderScreenMixin}'s render-list based alignment, which does not depend on that (mixin-injected)
 * method existing. Kept as a plain placeholder so the file name stays stable.
 */
final class GuiExPatternProviderLayoutMixin {

    private GuiExPatternProviderLayoutMixin() {
    }
}
