package com.heypixel.heypixelmod.modules.impl.render;


import com.heypixel.heypixelmod.modules.Category;
import com.heypixel.heypixelmod.modules.Module;
import com.heypixel.heypixelmod.modules.ModuleInfo;
import com.heypixel.heypixelmod.values.ValueBuilder;
import com.heypixel.heypixelmod.values.impl.ModeValue;
import net.minecraft.resources.ResourceLocation;

@ModuleInfo(
        name = "Cape",
        description = "Custom cape module",
        category = Category.RENDER
)
public class Cape extends Module {
    private final ModeValue styleValue = ValueBuilder.create(this, "Style")
            .setModes("Bilibili", "Jiaran", "Staff", "CherryBlossom", "Ba", "C", "C1", "Cat", "Cat2", "Cs", "M", "O1", "O2", "Qx2", "Vape")
            .setDefaultModeIndex(2)
            .build()
            .getModeValue();

    public Cape() {
        this.setEnabled(true);
    }

    public ResourceLocation getCapeLocation() {
        return CapeStyle.valueOf(this.styleValue.getCurrentMode().toUpperCase()).location;
    }

    @Override
    public String getSuffix() {
        return this.styleValue.getCurrentMode();
    }

    public static enum CapeStyle {
        BILIBILI(ResourceLocation.bySeparator("navenxd:capes/bilibili.png", ':')),
        JIARAN(ResourceLocation.bySeparator("navenxd:capes/jiaran.png", ':')),
        STAFF(ResourceLocation.bySeparator("navenxd:capes/staff.png", ':')),
        CHERRYBLOSSOM(ResourceLocation.bySeparator("navenxd:capes/cherryblossom.png", ':')),
        BA(ResourceLocation.bySeparator("navenxd:capes/ba.png", ':')),
        C(ResourceLocation.bySeparator("navenxd:capes/c.png", ':')),
        C1(ResourceLocation.bySeparator("navenxd:capes/c1.png", ':')),
        CAT(ResourceLocation.bySeparator("navenxd:capes/cat.png", ':')),
        CAT2(ResourceLocation.bySeparator("navenxd:capes/cat2.png", ':')),
        CS(ResourceLocation.bySeparator("navenxd:capes/cs.png", ':')),
        M(ResourceLocation.bySeparator("navenxd:capes/m.png", ':')),
        O1(ResourceLocation.bySeparator("navenxd:capes/o1.png", ':')),
        O2(ResourceLocation.bySeparator("navenxd:capes/o2.png", ':')),
        QX2(ResourceLocation.bySeparator("navenxd:capes/qx2.png", ':')),
        VAPE(ResourceLocation.bySeparator("navenxd:capes/vape.png", ':'));

        private final ResourceLocation location;

        private CapeStyle(ResourceLocation location) {
            this.location = location;
        }
    }
}