**Assumptions**

* The sliding windows rate limiter will NOT count rejected requests into the current window.
* Only ONE market data processor will process each particular set of market data, 
  ie no two processors will be subscribing to the same symbol.
* If an request is rejected either by the rate limiter check or the symbol check, it will be ignored for now.
  Depending on the frequency of update of that asset, further action will needed to be taken. 
* 

