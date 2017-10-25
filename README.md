# org.openhab.binding.zonky
This is a OH1.x binding accessing statistics values from Zonky (http://www.zonky.cz)

# items file example:
```
Number ZonkyBalance "Zonky balance [%.2f Kč]" <money> (FF_Money) { zonky="balance" }
Number ZonkyDispoBalance "Zonky k dispozici [%.2f Kč]" <money> (FF_Money) { zonky="availableBalance" }
Number ZonkyBlockedBalance "Zonky blokovano [%.2f Kč]" <money> (FF_Money) { zonky="blockedBalance" }
Number ZonkyCreditSum "Zonky investovano [%.2f Kč]" <money> (FF_Money) { zonky="creditSum" }
Number ZonkyDebitSum "Zonky vybrano [%.2f Kč]" <money> (FF_Money) { zonky="debitSum" }
Number ZonkyCurProfit "Aktualni zisk [%.2f %%]" <money> (FF_Money) { zonky="currentProfitability" }
Number ZonkyExpProfit "Ocekavany zisk [%.2f %%]" <money> (FF_Money) { zonky="expectedProfitability" }
Number ZonkyCurInvCount "Aktivnich investic [%d]" <money> (FF_Money) {zonky="currentOverview.investmentCount" }
Number ZonkyCurTotalInv "Zonky aktualni investice [%.2f Kč]" <money> (FF_Money) { zonky="currentOverview.totalInvestment" }
Number ZonkyPrincipalPaid "Zaplacena jistina [%.2f Kč]" <money> (FF_Money) { zonky="currentOverview.principalPaid" }
Number ZonkyInterestPaid "Zaplaceny urok [%.2f Kč]" <money> (FF_Money) { zonky="currentOverview.interestPaid" }
Number ZonkyPrincipalLeftDue "Jistina po splatnosti [%.2f Kč]" <money> (FF_Money) { zonky="currentOverview.principalLeftDue" }
Number ZonkyInterestLeftDue "Urok po splatnosti [%.2f Kč]" <money> (FF_Money) { zonky="currentOverview.interestLeftDue" }
Number ZonkyOverTotalInv "Zonky celkova investice [%.2f Kč]" <money> (FF_Money) { zonky="overallOverview.totalInvestment" }
Number ZonkyWPaidInstalments "Splatek za tyden [%d]" <money> (FF_Money) { zonky="weekly.paidInstalments" }
Number ZonkyWPaidInstalmentsAmount "Splaceno za tyden [%.2f Kč]" <money> (FF_Money) { zonky="weekly.paidInstalmentsAmount" }
```