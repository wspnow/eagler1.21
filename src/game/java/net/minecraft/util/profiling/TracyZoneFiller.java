package net.minecraft.util.profiling;

//import com.mojang.jtracy.Plot;
//import com.mojang.jtracy.TracyClient;
import com.mojang.logging.LogUtils;
import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import net.minecraft.util.profiling.metrics.MetricCategory;
import org.slf4j.Logger;

public class TracyZoneFiller implements ProfilerFiller {
    @Override
    public void startTick() {
    }

    @Override
    public void endTick() {
    }

    @Override
    public void push(String p_364548_) {
    }

    @Override
    public void push(Supplier<String> p_367014_) {
    }

    @Override
    public void pop() {
    }

    @Override
    public void popPush(String p_362480_) {
    }

    @Override
    public void popPush(Supplier<String> p_368969_) {
    }

    @Override
    public void markForCharting(MetricCategory p_360953_) {
    }

    @Override
    public void incrementCounter(String p_362137_, int p_362577_) {
    }

    @Override
    public void incrementCounter(Supplier<String> p_362628_, int p_368047_) {
    }

    @Override
    public void addZoneText(String p_362912_) {
    }

    @Override
    public void addZoneValue(long p_366154_) {
    }

    @Override
    public void setZoneColor(int p_363144_) {
    }
}

