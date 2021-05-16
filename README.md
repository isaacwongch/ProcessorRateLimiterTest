**Assumptions**

* The sliding windows rate limiter will NOT count rejected requests into the current window.
* Only ONE market data processor will process each particular set of market data, 
  ie no two processors will be subscribing to the price update of the same symbol.
* If an request is rejected either by the rate limiter check or the symbol check, it will be ignored for now.
* The algorithm used: 
  > Used the approximate algorithm to calculate the number of requests within that window
  > R(t) = Rp x (1000 - timeElapsedInCurrentWindow)/1000 + Rc should be < 100                                  
* Upon calling `publishAggregatedMarketData`, market data of that symbol should carry the latest info, which is 
  dictated by the field `updateTime`

**Further Thought**
* The order of `rateLimiter.isAllowed()` and `isSymbolAllowed()`:
  * if it passed `rateLimiter.isAllowed()` but not `isSymbolAllowed()` then a permit is wasted. 
  (Depends on the frequency of same symbol update, update from market provider or exchange?)
  * if it passed `isSymbolAllowed()` but not `rateLimiter.isAllowed()` then the update is recorded but not reflected in the DB.
    * If scenario like `T1: Symbol:MSFT updateTime: 14:02:02:00` & `T2: Symbol:MSFT updateTime: 14:02:01:00` then it might be possible.
* Reduce memory usage in the rate limiter --> ExpiryMap()? (https://github.com/jhalterman/expiringmap) 
* How to handle rejected requests?
* Use of BloomFilter (https://github.com/RedisBloom/RedisBloom) to replace for the symbol allowance check?
* CMH parameters load factor
