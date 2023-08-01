package igentuman.bfr.datagen.tag;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagBuilder;

import java.util.function.Function;
import java.util.function.Supplier;

//Based off of IntrinsicHolderTagsProvider.IntrinsicTagAppender but with a few shortcuts for forge registry entries and also a few more helpers and addition of SafeVarargs annotations
public class IntrinsicMekanismTagBuilder<TYPE> extends MekanismTagBuilder<TYPE, IntrinsicMekanismTagBuilder<TYPE>> {

    private final Function<TYPE, ResourceKey<TYPE>> keyExtractor;

    public IntrinsicMekanismTagBuilder(Function<TYPE, ResourceKey<TYPE>> keyExtractor, TagBuilder builder, String modID) {
        super(builder, modID);
        this.keyExtractor = keyExtractor;
    }

    @SafeVarargs
    public final IntrinsicMekanismTagBuilder<TYPE> add(Supplier<TYPE>... elements) {
        return addTyped(Supplier::get, elements);
    }

    private ResourceLocation getKey(TYPE element) {
        return keyExtractor.apply(element).location();
    }

    @SafeVarargs
    public final IntrinsicMekanismTagBuilder<TYPE> add(TYPE... elements) {
        return add(this::getKey, elements);
    }

    @SafeVarargs
    public final <T> IntrinsicMekanismTagBuilder<TYPE> addTyped(Function<T, TYPE> converter, T... elements) {
        return add(converter.andThen(this::getKey), elements);
    }

    @SafeVarargs
    public final IntrinsicMekanismTagBuilder<TYPE> addOptional(TYPE... elements) {
        return addOptional(this::getKey, elements);
    }

    @SafeVarargs
    public final IntrinsicMekanismTagBuilder<TYPE> remove(TYPE... elements) {
        return remove(this::getKey, elements);
    }
}