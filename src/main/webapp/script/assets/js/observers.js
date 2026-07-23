/*
	Name:	observers.js
	Version:	2.2.0

	Description:
	require MutationObserver( supports from IE11 and modern browsers )

	Note:
		ClassWatcher
		Map( Polyfill from ES6 Map to ES3 )
		ComputedStyleObserver

	Modified	(YYYY/MM/DD)	Ver		Content
	jbaek		2020/03/30		2.2.0	create
*/


/***********************************************************************************************************************
	ClassWatcher
***********************************************************************************************************************/
function ClassWatcher( targetNode, classToWatch, classAddedCallback, classRemovedCallback ) {
	this.targetNode = targetNode;
	this.classToWatch = classToWatch;
	this.classAddedCallback = classAddedCallback;
	this.classRemovedCallback = classRemovedCallback;
	this.observer = null;
	this.lastClassState = targetNode.classList.contains( this.classToWatch );
	this.init();
}
ClassWatcher.prototype.init = function() {
	this.observer = new MutationObserver( ClassWatcher.createMutationsCallback(this) );
	this.observe();
};
ClassWatcher.prototype.observe = function() {
	this.observer.observe( this.targetNode, { attributes: true } );
};
ClassWatcher.prototype.disconnect = function() {
	this.observer.disconnect();
};
ClassWatcher.createMutationsCallback = function( refObj ) {
	return function( mutationsList, observer ) {
		var ref = refObj;
		if( mutationsList && mutationsList.length > 0 ) {
			for( var i =0; i < mutationsList.length; i++ ) {
				var mutation = mutationsList[i];
				if( mutation.type === 'attributes' && mutation.attributeName === 'class' ) {
					var currentClassState = mutation.target.classList.contains( ref.classToWatch );
					if( ref.lastClassState !== currentClassState ) {
						ref.lastClassState = currentClassState;
						if( currentClassState ) {
							ref.classAddedCallback( mutation );
						} else {
							ref.classRemovedCallback( mutation );
						}
					}
				}
			}
		}
	};
};


/***********************************************************************************************************************
	Map(Polyfill from ES6 Map to ES3)
***********************************************************************************************************************/
"use strict";
function _instanceof(left, right) { if (right != null && typeof Symbol !== "undefined" && right[Symbol.hasInstance]) { return !!right[Symbol.hasInstance](left); } else { return left instanceof right; } }
//anonyco
if( typeof Map == 'undefined' || !
	/*window.*/
	Map.prototype.keys || typeof Set == 'undefined' || !
	/*window.*/
	Set.prototype.keys ) (function () {
		'use-strict';
		var keycur,
			i,
			len,
			k,
			v,
			iterable,
			Mapproto = {
				//length: 0,
				'delete': function _delete(key) {
					keycur = NaNsearch(this.k, key); // k is for keys
					if (!~keycur) return false;
					this.k.splice(keycur, 1);
					this.v.splice(keycur, 1);
					--this.size;
					return true;
				},
				'get': function get(key) {
					return this.v[NaNsearch(this.k, key)]; // automagicly returns undefined if it doesn't exist
				},
				'set': function set(key, value) {
					keycur = NaNsearch(this.k, key);
					if (!~keycur) // if (keycur === -1)
						this.k[keycur = this.size++] = key;
					this.v[keycur] = value;
					return this;
				},
				'has': function has(key) {
					return NaNsearch(this.k, key) > -1;
				},
				'clear': function clear() {
					this.k.length = this.v.length = this.size = 0; //return undefined
				},
				'forEach': function forEach(Func, thisArg) {
					if (thisArg) Func = Func.bind(thisArg);
					var i = -1,
						len = this.size;
					while (++i !== len) {
						Func(this.v[i], this.k[i], this);
					}
				},
				'entries': function entries() {
					var nextIndex = 0,
						that = this;
					return {
						next: function next() {
							return nextIndex !== that.size ? {
								value: [that.k[nextIndex++], that.v[nextIndex]],
								done: false
							} : {
								done: true
							};
						}
					};
				},
				'keys': function keys() {
					var nextIndex = 0,
						that = this;
					return {
						next: function next() {
							return nextIndex !== that.size ? {
								value: that.k[nextIndex++],
								done: false
							} : {
								done: true
							};
						}
					};
				},
				'values': function values() {
					var nextIndex = 0,
						that = this;
					return {
						next: function next() {
							return nextIndex !== that.size ? {
								value: that.v[nextIndex++],
								done: false
							} : {
								done: true
							};
						}
					};
				},
				toString: function toString() {
					return '[object Map]';
				}
			};

		function NaNsearch(arr, val) {
			// search that compensates for NaN indexs
			if (val === val) // if val is not NaN
				return arr.indexOf(val);
			i = 0, len = arr.length; // Check for the first index that is not itself (i.e. NaN)
			while (arr[i] === arr[i] && ++i !== len) {
				;
			} // do nothing

			return i;
		}

		; // Map & WeakMap polyfill

		/*window.*/

		WeakMap =
			/*window.*/
		Map = function Map(raw) {
			k = this.k = [];
			v = this.v = [];
			len = 0;

			if (raw !== undefined && raw !== null) {
				iterable = Object(raw); // split up the data into two useable streams: one for keys (k), and one for values (v)

				i = +iterable.length;
				if (i != i) // if i is NaN
					throw new TypeError('(' + (raw.toString || iterable.toString)() + ') is not iterable');

				while (i--) {
					if (_instanceof(iterable[i], Object)) {
						if (!~NaNsearch(k, iterable[i][0])) // if current is not already in the array
							k[len] = iterable[i][0], v[len++] = iterable[i][1]; // len++ increments len, but returns value before increment
					} else throw new TypeError('Iterator value ' + iterable[i] + ' is not an entry object');
				}

				k.reverse();
				v.reverse();
			}

			this.size = len;
		};
		/*window.*/


		Map.prototype = Mapproto;
		/*if (typeof Symbol === 'function'){
		  Map.prototype[Symbol.iterator] = Map.prototype.values;
		  Map.prototype[Symbol.toStringTag] = 'Map';
		  }*/
		// Set & WeakSet polyfill

		/*window.*/

		WeakSet =
			/*window.*/
		Set = function Set(raw) {
			k = this.k = this.v = [];
			len = 0;

			if (raw !== undefined && raw !== null) {
				iterable = Object(raw); // split up the data into two useable streams: one for keys (k), and one for values (v)

				i = +iterable.length;
				if (i != i) // if i is NaN
					throw new TypeError('(' + (raw.toString || iterable.toString)() + ') is not iterable');

				while (i--) {
					if (!~NaNsearch(k, iterable[i])) // if current is not already in the array
						k[len++] = iterable[i];
				} // len++ increments len, but returns value before increment


				k.reverse();
			}

			this.size = len;
		};
		/*window.*/


		Set.prototype = {
			//length: 0,
			'delete': function _delete(value) {
				keycur = NaNsearch(this.k, value); // k is for keys

				if (!~keycur) return false;
				this.k.splice(keycur, 1);
				--this.size;
				return true;
			},
			'add': function add(value) {
				keycur = NaNsearch(this.k, value);
				if (!~keycur) keycur = this.size++;
				this.k[keycur] = value;
				return this;
			},
			'has': Mapproto.has,
			'clear': Mapproto.clear,
			'forEach': Mapproto.forEach,
			'entries': Mapproto.entries,
			'keys': Mapproto.keys,
			'values': Mapproto.keys,
			toString: function toString() {
				return '[object Set]';
			}
		};
		/*if (typeof Symbol === 'function'){
		  Set.prototype[Symbol.iterator] = Set.prototype.values;
		  Set.prototype[Symbol.toStringTag] = 'Set';
		  Set.prototype[Symbol.toPrimitive] = function(){return this.k};
		  }*/
	})();


/***********************************************************************************************************************
	ComputedStyleObserver
***********************************************************************************************************************/
function _instanceof(left, right) { if (right != null && typeof Symbol !== "undefined" && right[Symbol.hasInstance]) { return !!right[Symbol.hasInstance](left); } else { return left instanceof right; } }

function _toConsumableArray(arr) { return _arrayWithoutHoles(arr) || _iterableToArray(arr) || _nonIterableSpread(); }

function _nonIterableSpread() { throw new TypeError("Invalid attempt to spread non-iterable instance"); }

function _iterableToArray(iter) { if (Symbol.iterator in Object(iter) || Object.prototype.toString.call(iter) === "[object Arguments]") return Array.from(iter); }

function _arrayWithoutHoles(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = new Array(arr.length); i < arr.length; i++) { arr2[i] = arr[i]; } return arr2; } }

function _classCallCheck(instance, Constructor) { if (!_instanceof(instance, Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } }

function _createClass(Constructor, protoProps, staticProps) { if (protoProps) _defineProperties(Constructor.prototype, protoProps); if (staticProps) _defineProperties(Constructor, staticProps); return Constructor; }

(function () {
	"use strict";

	var a = 1,
		b = 0;
	var c = b,
		d = new Map();

	var e = function e(b, _e) {
		var f = d.get(b);
		return (f || (f = {
			styles: {},
			observers: []
		}, d.set(b, f)), !f.observers.includes(_e)) && (f.observers.push(_e), c !== a && (c = a, j()), !0);
	},
		f = function f(a, e) {
			var f = d.get(a);
			if (!f) return !1;
			var g = f.observers.indexOf(e);
			return -1 !== g && (f.observers.splice(g, 1), 0 === f.observers.length && d.delete(a), 0 === d.size && (c = b), !0);
		},
		g = function g(a) {
			d.forEach(function (b, c) {
				b.observers.includes(a) && f(c, a);
			});
		},
		h = function h() {
			d.forEach(function (a, b) {
				i(b, a);
			});
		},
		i = function i(a, b) {
			var c = getComputedStyle(a),
				d = {};
			b.observers.forEach(function (e) {
				var f = [];
				e.properties.forEach(function (e) {
					var g = c[e],
						h = b.styles[e];
					g !== h && h && f.push(new ComputedStyleObserverEntry(a, e, g, h)), d[e] = g;
				}), f.length && e.callback(f);
			}), b.styles = d;
		},
		j = function j() {
			c === a && (requestAnimationFrame(j), h());
		};

	var k = new WeakMap();
	window.ComputedStyleObserver =
		/*#__PURE__*/
	function () {
		function _class(a) {
			var b = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : null;

			_classCallCheck(this, _class);

			Array.isArray(b) && (b = _toConsumableArray(b)), k.set(this, {
				callback: a,
				properties: b
			});
		}

		_createClass(_class, [{
			key: "disconnect",
			value: function disconnect() {
				g(k.get(this));
			}
		}, {
			key: "observe",
			value: function observe(a) {
				return e(a, k.get(this));
			}
		}, {
			key: "unobserve",
			value: function unobserve(a) {
				return f(a, k.get(this));
			}
		}]);

		return _class;
	}(), window.ComputedStyleObserverEntry =
		/*#__PURE__*/
	function () {
		function _class2(a, b, c, d) {
			_classCallCheck(this, _class2);

			this.target = a, this.property = b, this.value = c, this.previousValue = d;
		}

		return _class2;
	}();
})();
