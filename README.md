**Assumptions**

* The sliding windows rate limiter will NOT count rejected requests into the current window.
* Only ONE market data processor will process each particular set of market data, 
  ie no two processors will be subscribing to the same symbol.
* 

