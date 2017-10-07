function getUrlVars() {
    var vars = {};
    var parts = window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi,    
    function(m,key,value) {
      vars[key] = value;
    });
    return vars;
}

var keyStr = "ABCDEFGHIJKLMNOP" +
			"QRSTUVWXYZabcdef" +
			"ghijklmnopqrstuv" +
			"wxyz0123456789+-" +
			"=";

function encode64(input) {
	input = escape(input);
	var output = "";
	var chr1, chr2, chr3 = "";
	var enc1, enc2, enc3, enc4 = "";
	var i = 0;
	
	do {
		chr1 = input.charCodeAt(i++);
		chr2 = input.charCodeAt(i++);
		chr3 = input.charCodeAt(i++);
		
		enc1 = chr1 >> 2;
		enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
		enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
		enc4 = chr3 & 63;
		
		if (isNaN(chr2)) {
			enc3 = enc4 = 64;
		} else if (isNaN(chr3)) {
			enc4 = 64;
		}
		
		output = output +
		keyStr.charAt(enc1) +
		keyStr.charAt(enc2) +
		keyStr.charAt(enc3) +
		keyStr.charAt(enc4);
		chr1 = chr2 = chr3 = "";
		enc1 = enc2 = enc3 = enc4 = "";
	} while (i < input.length);
	
	return output;
}

function decode64(input) {
	var output = "";
	var chr1, chr2, chr3 = "";
	var enc1, enc2, enc3, enc4 = "";
	var i = 0;
	
	// remove all characters that are not A-Z, a-z, 0-9, +, /, or =, or -
//	var base64test = /[^A-Za-z0-9\+\/\=\-]/g;
//	if (base64test.exec(input)) {
//		alert("There were invalid base64 characters in the input text.\n" +
//		"Valid base64 characters are A-Z, a-z, 0-9, '+', '/',and '='\n" +
//		"Expect errors in decoding.");
//	}
//	input = input.replace(/[^A-Za-z0-9\+\/\=]/g, "");
	
	do {
		enc1 = keyStr.indexOf(input.charAt(i++));
		enc2 = keyStr.indexOf(input.charAt(i++));
		enc3 = keyStr.indexOf(input.charAt(i++));
		enc4 = keyStr.indexOf(input.charAt(i++));
		
		chr1 = (enc1 << 2) | (enc2 >> 4);
		chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
		chr3 = ((enc3 & 3) << 6) | enc4;
		
		output = output + String.fromCharCode(chr1);
		
		if (enc3 != 64) {
			output = output + String.fromCharCode(chr2);
		}
		if (enc4 != 64) {
			output = output + String.fromCharCode(chr3);
		}
		
		chr1 = chr2 = chr3 = "";
		enc1 = enc2 = enc3 = enc4 = "";
	
	} while (i < input.length);
	
	return unescape(output);
}

function parse_url(str, component) {
	  //       discuss at: http://phpjs.org/functions/parse_url/
	  //      original by: Steven Levithan (http://blog.stevenlevithan.com)
	  // reimplemented by: Brett Zamir (http://brett-zamir.me)
	  //         input by: Lorenzo Pisani
	  //         input by: Tony
	  //      improved by: Brett Zamir (http://brett-zamir.me)
	  //             note: original by http://stevenlevithan.com/demo/parseuri/js/assets/parseuri.js
	  //             note: blog post at http://blog.stevenlevithan.com/archives/parseuri
	  //             note: demo at http://stevenlevithan.com/demo/parseuri/js/assets/parseuri.js
	  //             note: Does not replace invalid characters with '_' as in PHP, nor does it return false with
	  //             note: a seriously malformed URL.
	  //             note: Besides function name, is essentially the same as parseUri as well as our allowing
	  //             note: an extra slash after the scheme/protocol (to allow file:/// as in PHP)
	  //        example 1: parse_url('http://username:password@hostname/path?arg=value#anchor');
	  //        returns 1: {scheme: 'http', host: 'hostname', user: 'username', pass: 'password', path: '/path', query: 'arg=value', fragment: 'anchor'}

	  var query, key = ['source', 'scheme', 'authority', 'userInfo', 'user', 'pass', 'host', 'port',
	      'relative', 'path', 'directory', 'file', 'query', 'fragment'
	    ],
	    ini = (this.php_js && this.php_js.ini) || {},
	    mode = (ini['phpjs.parse_url.mode'] &&
	      ini['phpjs.parse_url.mode'].local_value) || 'php',
	    parser = {
	      php: /^(?:([^:\/?#]+):)?(?:\/\/()(?:(?:()(?:([^:@]*):?([^:@]*))?@)?([^:\/?#]*)(?::(\d*))?))?()(?:(()(?:(?:[^?#\/]*\/)*)()(?:[^?#]*))(?:\?([^#]*))?(?:#(.*))?)/,
	      strict: /^(?:([^:\/?#]+):)?(?:\/\/((?:(([^:@]*):?([^:@]*))?@)?([^:\/?#]*)(?::(\d*))?))?((((?:[^?#\/]*\/)*)([^?#]*))(?:\?([^#]*))?(?:#(.*))?)/,
	      loose: /^(?:(?![^:@]+:[^:@\/]*@)([^:\/?#.]+):)?(?:\/\/\/?)?((?:(([^:@]*):?([^:@]*))?@)?([^:\/?#]*)(?::(\d*))?)(((\/(?:[^?#](?![^?#\/]*\.[^?#\/.]+(?:[?#]|$)))*\/?)?([^?#\/]*))(?:\?([^#]*))?(?:#(.*))?)/ // Added one optional slash to post-scheme to catch file:/// (should restrict this)
	    };

	  var m = parser[mode].exec(str),
	    uri = {},
	    i = 14;
	  while (i--) {
	    if (m[i]) {
	      uri[key[i]] = m[i];
	    }
	  }

	  if (component) {
	    return uri[component.replace('PHP_URL_', '')
	      .toLowerCase()];
	  }
	  if (mode !== 'php') {
	    var name = (ini['phpjs.parse_url.queryKey'] &&
	      ini['phpjs.parse_url.queryKey'].local_value) || 'queryKey';
	    parser = /(?:^|&)([^&=]*)=?([^&]*)/g;
	    uri[name] = {};
	    query = uri[key[12]] || '';
	    query.replace(parser, function($0, $1, $2) {
	      if ($1) {
	        uri[name][$1] = $2;
	      }
	    });
	  }
	  delete uri.source;
	  return uri;
	}


	function simpleHttpRequest(url, success, failure) {
		  var request = makeHttpObject();
		  request.open("GET", url, true);
		  request.send(null);
		  request.onreadystatechange = function() {
		    if (request.readyState == 4) {
		      if (request.status == 200)
		        success(request.responseText);
		      else if (failure)
		        failure(request.status, request.statusText);
		    }
		  };
	}
	
	function makeHttpObject() {
		  try {return new XMLHttpRequest();}
		  catch (error) {}
		  try {return new ActiveXObject("Msxml2.XMLHTTP");}
		  catch (error) {}
		  try {return new ActiveXObject("Microsoft.XMLHTTP");}
		  catch (error) {}

		  throw new Error("Could not create HTTP request object.");
	}
	
	function sendMail() {
		
		var msg = document.getElementById("mail_msg").value;
		var tom = document.getElementById("mail_to").value;
		var nom = document.getElementById("name_to").value;
		
		document.getElementById("sendMailBtn").disabled = true;
		document.getElementById("mail_msg").value = '\r\nSending.. \r\n\r\nPlease wait.';
		document.getElementById("mail_to").value = "";
		document.getElementById("name_to").value = "";
		document.getElementById("mail_msg").disabled = true;
		document.getElementById("mail_to").disabled = true;
		document.getElementById("name_to").disabled = true;
		
		msg += " \r\n*****\r\nmessage from: " +  tom + " name: " + nom;
		
//		msg = msg.replace("@", "%40");
		
		if(msg && msg.length && tom && tom.length && tom.indexOf("@")>0) { // email service does not works without billing
			var urlStr = "sendmail?subj=ECG for Everybody Message&msg=" + msg + 
			"&to=stevan@ecg4everybody.com&from=service.info@ecg4everybody.com";
			
			simpleHttpRequest(urlStr, mailResponse, mailResponse)
//			console.log('urlStr: ' + urlStr);
		}
		else {
			alert('Bad mail parameters');
		}
	}
	
	function mailResponse(response) {
		
		document.getElementById("sendMailBtn").disabled = false;
		document.getElementById("mail_msg").disabled = false;
		document.getElementById("mail_to").disabled = false;
		document.getElementById("name_to").disabled = false;
		console.log("mailResponse: " + response);
		if(response && (response.toLowerCase().indexOf("ok".toLowerCase()) ) >= 0 ) {
			document.getElementById("mail_msg").value = "Message sent, thanks for your mail.";
		}
		else {
			document.getElementById("mail_msg").value = "Error ond mail sending, please try later, or send e-mail to service.info@ecg4everybody.com.";
//				document.getElementById("mail_msg").value = response;
			console.log("Error ond mail sending: " + response);
		}
		document.getElementById("mail_to").value = "";
		document.getElementById("name_to").value = "";
	}
	
	function validateEmail(email) {
	    var re = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
	    return re.test(email);
	}

	function isMobile() {
		if(/(android|bb\d+|meego).+mobile|avantgo|bada\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|ipad|iris|kindle|Android|Silk|lge |maemo|midp|mmp|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\.(browser|link)|vodafone|wap|windows (ce|phone)|xda|xiino/i.test(navigator.userAgent) 
			    || /1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\-(n|u)|c55\/|capi|ccwa|cdm\-|cell|chtm|cldc|cmd\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\-s|devi|dica|dmob|do(c|p)o|ds(12|\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\-|_)|g1 u|g560|gene|gf\-5|g\-mo|go(\.w|od)|gr(ad|un)|haie|hcit|hd\-(m|p|t)|hei\-|hi(pt|ta)|hp( i|ip)|hs\-c|ht(c(\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\-(20|go|ma)|i230|iac( |\-|\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\/)|klon|kpt |kwc\-|kyo(c|k)|le(no|xi)|lg( g|\/(k|l|u)|50|54|\-[a-w])|libw|lynx|m1\-w|m3ga|m50\/|ma(te|ui|xo)|mc(01|21|ca)|m\-cr|me(rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\-2|po(ck|rt|se)|prox|psio|pt\-g|qa\-a|qc(07|12|21|32|60|\-[2-7]|i\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\-|oo|p\-)|sdk\/|se(c(\-|0|1)|47|mc|nd|ri)|sgh\-|shar|sie(\-|m)|sk\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\-|v\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\-|tdg\-|tel(i|m)|tim\-|t\-mo|to(pl|sh)|ts(70|m\-|m3|m5)|tx\-9|up(\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|yas\-|your|zeto|zte\-/i.test(navigator.userAgent.substr(0,4))) 
			return true;
		else return false;
	}
