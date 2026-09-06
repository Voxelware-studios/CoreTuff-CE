package org.voxelware.coretuff.api.economy;

import java.util.List;
import java.util.UUID;

/**
 * CoreTuff Economy API — high-level interface for economy management.
 * <p>
 * Thread safety:
 * <ul>
 *   <li>{@link #getBalance(UUID)}, {@link #hasBalance(UUID, double)}, {@link #getTopBalances(int)},
 *       {@link #format(double)}, {@link #getCurrencyName()}, {@link #getCurrencySymbol()} —
 *       safe to call from any thread.</li>
 *   <li>{@link #deposit(UUID, double, String)}, {@link #withdraw(UUID, double, String)},
 *       {@link #transfer(UUID, UUID, double, String)} — perform I/O; call async or off the main thread.</li>
 * </ul>
 */
public interface EconomyApi {

    double getBalance(UUID player);

    boolean hasBalance(UUID player, double amount);

    EconomyResult deposit(UUID player, double amount, String reason);

    EconomyResult withdraw(UUID player, double amount, String reason);

    EconomyResult transfer(UUID from, UUID to, double amount, String reason);

    String format(double amount);

    String getCurrencyName();

    String getCurrencySymbol();

    List<BalanceEntry> getTopBalances(int amount);
}
