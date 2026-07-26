package cn.ae2bc.extension;

import appeng.api.upgrades.IUpgradeInventory;
import appeng.helpers.externalstorage.GenericStackInv;
import cn.ae2bc.logic.ProductExtractionSettings;
import cn.ae2bc.logic.ProductExtractionTickState;

public interface PatternProviderExtractionExtension {
    IUpgradeInventory ae2bc$getProductExtractionUpgrades();

    GenericStackInv ae2bc$getProductExtractionMarkers();

    ProductExtractionSettings ae2bc$getProductExtractionSettings();

    void ae2bc$setProductExtractionInterval(int interval);

    void ae2bc$setProductExtractionAmount(int amount);

    void ae2bc$setProductExtractionWhitelist(boolean whitelist);

    boolean ae2bc$hasProductExtractionCard();

    ProductExtractionTickState ae2bc$tickProductExtraction();
}
