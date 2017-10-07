var loginUUID = null;
var userEmail = null, userId = null;
var logedInuser = null;
var mobileAccess = false;

function initCS() {
	
	if (!navigator.cookieEnabled) {
		$("#registerBtn").hide();
		$("#loginBtn").hide();
		$("#logoutBtn").hide();
		
		$("#userLab").show();
		$("#userSpinner").hide();
		$("#userLab").text('This site requires cookies, please enable it in your browser.');
		
		return;
	}
	
	loginUUID = getCookie('login');
	userEmail = getCookie('email');
	userId = getCookie('userid');
	console.log('read cookie userEmail: ' + userEmail + ' userId: ' + userId + ' loginUUID: ' + loginUUID);
	
	if(loginUUID) {
		getUserForUUID(loginUUID);
	}
	else {
		$("#registerBtn").show();
		$("#loginBtn").show();
		$("#logoutBtn").hide();
		$("#userSpinner").hide();
	}
	
	mobileAccess = isMobile();
	console.log('mobileAccess: ' + mobileAccess);
	
	getData();
	
}


function getCookie(cname) {
    var name = cname + "=";
    var ca = document.cookie.split(';');
    for(var i=0; ca && i<ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0)==' ') c = c.substring(1);
        if (c.indexOf(name) == 0) return c.substring(name.length,c.length);
    }
    return "";
}

function setCookie(cname, cvalue, exdays) {
	try {
		var d = new Date();
	    d.setTime(d.getTime() + (exdays*24*60*60*1000));
	    var expires = "expires="+d.toUTCString();
	    document.cookie = cname + "=" + cvalue + "; " + expires;	
	} catch(e) {
		console.log('error setCookie: ' + e);
	}
}

function getUserForUUID(uuid) {
	$.get( "../login/getlogedinuser/" + uuid, processGetUserForUUID);
}

var cntChUid = 0;
function processGetUserForUUID(result) {
	try {
		console.log('processGetUserForUUID: ' + loginUUID + ' res: ' + JSON.stringify(result))
//		logedInuser = JSON.parse(result);
		if(result && result.hasOwnProperty('email')) {
			logedInuser = new Object();
			logedInuser.email = result.email; 
			userEmail = result.email;
			logedInuser.id = result.id;
			logedInuser.name = result.name;
			logedInuser.verified = result.verified;
			
			$("#userLab").text(logedInuser.email.substring(0, logedInuser.email.indexOf('@')));
			$("#userLab").show();
			
			$("#registerBtn").hide();
			$("#loginBtn").hide();
			$("#logoutBtn").show();
		}
		else {
			if(cntChUid++ < 4) {
				setTimeout(function (){
					getUserForUUID(getCookie('login'));
			    }, 1300);
			}
			else {
				$("#registerBtn").show();
				$("#loginBtn").show();
				$("#logoutBtn").hide();
				logedInuser = null;
				$("#userLab").hide();
				setCookie('login', '', 0);
				setCookie('email', '', 0);
				setCookie('userid', '', 0);
			}
			
		}
	} catch (e) {
		console.log('error parse processGetUserForUUID: ' + e + ' result:' + result + ' responseText' + result.responseText + ' .email' + result.email);
	}
	
	$("#userSpinner").hide();
}

/// LOGIN
var inLoginProcess = false;
function openLoginModal() {
	
	$('#registerModal').modal('hide')
	
	$('#login_pwd').val('');
	
	if(userEmail) {
		$('#login_email').val(userEmail);
		setTimeout(function (){
	        $('#login_pwd').focus();
	    }, 1000);
	}
	else {
		setTimeout(function (){
	        $('#login_email').focus();
	    }, 1000);
	}
	
	$("#loginStatus").hide();
	$('#loginSpinner').hide();
	inLoginProcess = false;
	
	$('#loginModalBtn').prop('disabled', false);

	
    
	$('#login_pwd').keypress(function(e) {
        if(e.which == 13 && !inLoginProcess) {
//            jQuery(this).blur();
//            jQuery('#loginBtn').focus().click();
            login();
            return false;
        }
    });
	$('#login_email').keypress(function(e) {
        if(e.which == 13 && !inLoginProcess) {
//            jQuery(this).blur();
//            jQuery('#loginBtn').focus().click();
            login();
            return false;
        }
    });
}



function login() {
	
	$('#loginSpinner').show();
	$("#loginStatus").hide();
	$('#loginModalBtn').prop('disabled', true);
	
	inLoginProcess = true;
	
	var user ={	
			"pwd": $('#login_pwd').val(),
			"email": $('#login_email').val() }
	logedInuser = user;
	userEmail = user.email;
	
	$.post( "../login/remember", JSON.stringify(user), processLogin);
	
//	if ($('#rememberLoginCh').prop('checked')) {
//		$.post( "../login/remember", JSON.stringify(user), processLogin);
//	}
//	else { // returns html on server in localhost works fine
//		$.post( "../login/", JSON.stringify(user), processLogin);
		
//		 $.ajax({
//		      type: "POST",
//		      url: "../login/",
//		      data: JSON.stringify(user),
//		      contentType: "application/json",
//		      dataType: "text",
//		      processData: false, 
//		      success: processLogin,
//		      error: processLogin
//		    });
//	}
	 
	
}

function processLogin(result) {
	if ( result.responseText && result.responseText.indexOf("ok") > -1 || 
			(!result.responseText && result.indexOf("ok") > -1 ) ) {
		$("#loginStatus").text('Success');
		$("#loginStatus").attr("class", "alert alert-success");
		
		loginUUID = getCookie('login');
		
		if(logedInuser) setCookie('email', logedInuser.email, 365);
		userEmail = logedInuser.email;
		
		$("#loginBtn").fadeOut(400, function(){ $("#loginBtn").hide(); })
		$("#registerBtn").fadeOut(400, function(){ 
			$("#registerBtn").hide(); 
			$("#logoutBtn").show();
			$("#userLab").text(logedInuser.email.substring(0, logedInuser.email.indexOf('@')));
			$("#userLab").show();
			})
		$("#loginModal").fadeOut(900, function(){ $("#loginModal").modal('hide');  })
		
	}
	else {
		$("#loginStatus").text('Error, invalid e-mail or password');
		$("#loginStatus").attr("class", "alert alert-danger");
		$('#login_pwd').val('');
		logedInuser = null;
	}
	
	$('#loginSpinner').hide();
	$("#loginStatus").show();
	$('#loginModalBtn').prop('disabled', false);
	inLoginProcess = false;
}


function openRegisterModal() {
	
	$('#loginModal').modal('hide')
	$('#registerSpinner').hide();
	
	$('#register_pwd').val('');
	$('#repeat_register_pwd').val('');
	$("#registerStatus").hide();
	inLoginProcess = false;
	$('#registerModalBtn').prop('disabled', false);

	setTimeout(function (){
        $('#register_email').focus();
    }, 1000);
    
	$('#register_pwd').keypress(function(e) {
        if(e.which == 13 && !inLoginProcess) {
//            jQuery(this).blur();
//            jQuery('#loginBtn').focus().click();
            register();
            return false;
        }
    });
	$('#repeat_register_pwd').keypress(function(e) {
        if(e.which == 13 && !inLoginProcess) {
//            jQuery(this).blur();
//            jQuery('#loginBtn').focus().click();
            register();
            return false;
        }
    });
	$('#register_email').keypress(function(e) {
        if(e.which == 13 && !inLoginProcess) {
//            jQuery(this).blur();
//            jQuery('#loginBtn').focus().click();
        	register();
            return false;
        }
    });
}
var registrationUser = null;
function register() {
	
	$("#registerStatus").text('');
	$("#registerStatus").hide();
	
	
	if(!validateEmail($('#register_email').val())) {
		$("#registerStatus").text('Error, invalid e-mail');
		$("#registerStatus").attr("class", "alert alert-danger");
		$("#registerStatus").show();
		return;
	}

	if(! ($('#register_pwd').val() === $('#repeat_register_pwd').val()) ) {
		$("#registerStatus").text('Error, Password does not match the confirm password');
		$("#registerStatus").attr("class", "alert alert-danger");
		$("#registerStatus").show();
		return;
	}
	
	if($('#register_pwd').val().length < 6 ) {
		$("#registerStatus").text('Error, Password should be at least six characters');
		$("#registerStatus").attr("class", "alert alert-danger");
		$("#registerStatus").show();
		return;
	}
	
	$("#registerStatus").text('');
	$("#registerStatus").hide();
	inLoginProcess = true;
	
	registrationUser ={	
			"pwd": $('#register_pwd').val(),
			"email": $('#register_email').val() }
	
	$('#registerSpinner').show();
	$('#registerModalBtn').prop('disabled', true);
	$.post( "../e4euser", JSON.stringify(registrationUser), processRegister);
	
}

function processRegister(result) {
	
	if ( result.indexOf("ok") > -1 ) {
		$("#registerStatus").text('Success');
		$("#registerStatus").attr("class", "alert alert-success");
		
		setTimeout(function (){
			checkAddedRegistersdUser();
	    }, 2300);
	}
	else {
		$("#registerStatus").text('Error, e-mail already registered');
		$("#registerStatus").attr("class", "alert alert-danger");
		$('#register_pwd').val('');
		$('#repeat_register_pwd').val('');
		$('#registerSpinner').hide();
	}
	
	$("#registerStatus").show();
//	$('#registerSpinner').hide();
	inLoginProcess = false;
	$('#registerModalBtn').prop('disabled', false);
}

var testCnt = 0;
function checkAddedRegistersdUser() {
	$.get( "../e4euser/email/" + registrationUser.email, processCheckAddedRegistersdUser);
}

function processCheckAddedRegistersdUser(response) {
//	alert(JSON.stringify(response)); 
	if (response && response.hasOwnProperty('email')) { 
		console.log('realy added user, rsp: ' + JSON.stringify(response));
		loginAfterRegistration();
	}
	else {
		if(testCnt++ < 4) {
			console.log('CHECK added user, testCnt: ' + testCnt);
			setTimeout(function (){
				checkAddedRegistersdUser();
		    }, 1500);
		}
		else {
			setTimeout(function (){
				console.log('CHECK added user FAILED, testCnt: ' + testCnt);
				$('#registerModal').modal('hide');
		    }, 1000);
		}
		
	}
}

function loginAfterRegistration() {
	console.log('loginAfterRegistration..');
//	var user ={	
//			"pwd": $('#register_pwd').val(),
//			"email": $('#register_email').val() }
//	
//	alert('loginAfterRegistration: ' + JSON.stringify(user))
//	logedInuser = user;
	logedInuser = registrationUser;
	$.post( "../login/remember", JSON.stringify(logedInuser), processLoginAfterRegistration);
}

function processLoginAfterRegistration(result) {
//	alert('processLoginAfterRegistration: ' + result);
	console.log('loginAfterRegistration result: ' + result);
	$('#registerSpinner').hide();
	
	if ( result.indexOf("ok") > -1 ) {
		$("#registerStatus").text('Success Login');
		$("#registerStatus").attr("class", "alert alert-success");
		
		$("#loginBtn").hide();
		$("#registerBtn").hide();
		$("#logoutBtn").show();
		
		if(logedInuser && logedInuser.email) {
			$("#userLab").text(logedInuser.email.substring(0, logedInuser.email.indexOf('@')));
			$("#userLab").show();
		}		
		
		setTimeout(function (){
			loginUUID = getCookie('login');
			console.log('loginUUID in processLoginAfterRegistration: ' + loginUUID);
			userEmail = logedInuser.email;
			setCookie('email', logedInuser.email, 365);
		}, 1000);
		
//		setTimeout(function (){
//			getUserForUUID(loginUUID);
//	    }, 3300);
		
		
		setTimeout(function (){
			$('#registerModal').modal('hide');
	    }, 1500);
		
//		$("#loginBtn").fadeOut(400, function(){ $("#loginBtn").hide(); })
//		$("#registerBtn").fadeOut(400, function(){ 
//			$("#registerBtn").hide(); 
//			$("#logoutBtn").show();
//			if(logedInuser && logedInuser.email) {
//				$("#userLab").text(logedInuser.email.substring(0, logedInuser.email.indexOf('@')));
//				$("#userLab").show();
//			}
//			
//		})
//		$("#registerModal").fadeOut(1000, function(){ $("#registerModal").modal('hide');  })
		
	}
	else {
		logedInuser = null;
		loginUUID = null;
		$("#registerStatus").text('Error, invalid e-mail or password');
		$("#registerStatus").attr("class", "alert alert-danger");
		$('#login_pwd').val('');
		$("#registerModal").modal('hide');
	}
	
	$("#registerStatus").show();
}



function logout() {

	$.get( "../login/logout/", processLogout);
	
	setCookie('login', '', 0);
	setCookie('email', '', 0);
	setCookie('userid', '', 0); 
	$("#registerBtn").show();
	$("#loginBtn").show();
	$("#logoutBtn").hide();
	logedInuser = null;
	loginUUID = null;
	$("#userLab").hide();
}

function processLogout(result) {
//	alert(result);
	
//	$("#registerBtn").show();
//	$("#loginBtn").show();
//	$("#logoutBtn").hide();
//	logedInuser = null;
//	$("#userLab").hide();
	
//	if(result.indexOf("ok") > -1 ) {
//		$("#registerBtn").show();
//		$("#loginBtn").show();
//		$("#logoutBtn").hide();
//		logedInuser = null;
//		$("#userLab").hide();
//	}
//	else {
//		
//	}
}

function resetPwd() {
//	alert("reset");
	
	if(!$('#login_email').val()) {
		$("#loginStatus").attr("class", "alert alert-danger");
		$("#loginStatus").show();
		$("#loginStatus").text('Enter registrated e-mail');
		return;
	}
	
	$.get( "../e4euser/initresetpwd/" + $('#login_email').val(), processResetPwd);
	$("#loginStatus").hide();
	$('#loginSpinner').show();
}

function processResetPwd(response) {

	$('#loginSpinner').hide();
	
	if ( response.indexOf("ok") > -1 ) {
		$("#loginStatus").attr("class", "alert alert-success");
		$("#loginStatus").text('Success, you will receiwe e-mail with instructions how to reset pasword.');
	}
	else {
		$("#loginStatus").attr("class", "alert alert-danger");
		$("#loginStatus").text(response);
		
	}
	$("#loginStatus").show();
}

/// END LOGIN


/// GET DATA MESSAGES
var dataArrary;

var dataOffset=0, dataLimit = 10;
function getData() {
	$.get( "../e4edata/list/" + dataOffset + "/" + dataLimit, processGetData);
}

function processGetData(result) {
	
	dataArrary = result;
	
	$("#data_loading_spinner").hide();
	
	for (var i = 0; i < result.length; i++) {
		// container for user data (graphs and mess.) and crowdsourcing messages 
		$("#dataRows").append("<div id=\"dataRow"+i+"\" class=\"row\" style=\"border: 1px solid #AAA; max-height: 280px; overflow: auto; margin-bottom: .5cm;\"></div>");
//		$("#dataRows").append("<fieldset id=\"dataRow"+i+"\"  class=\"scheduler-border\"><legend class=\"scheduler-border\"><small>"+result[i].time + "</small></legend></fieldset>");
		
		// container for user graphs and user messages (left column)
		$("#dataRow"+i).append("<div id=\"user_container"+i+"\" class=\"col-xs-8\" style=\"overflow: auto; overflow-x: hidden; max-height: 255px;\"></div>");

		try {
			// sender note message with array data
			if(result[i].note && result[i].note.length) { // <span class=\"glyphicon glyphicon-pencil\">
				$("#user_container"+i).append("<div title=\"User message\"><i class=\"fa fa-comment\"></i> " + result[i].note+"</div>");
			}
		} catch (e) {
			console.log('error add user message: ' + e);
		}

		try {
			// byteArrays
			if (result[i].byteArrays != null && result[i].byteArrays.length) {
				for (var j = 0; j < result[i].byteArrays.length; j++) {
					$("#user_container" + i).append(
							"<div id=\"graph_b_" + i + "_" + j
									+ "\" style=\"height: 200px;\"></div>");
					createLineChart('graph_b_' + i + "_" + j,
							result[i].byteArrays[j].descr, result[i].time,
							result[i].byteArrays[j].fs, true, null,
							result[i].byteArrays[j].array); // '2015-01-04 06:06:30.0'
				}
			}
		} catch (e) { console.log('error add byte data: ' + e); }
		
		
		
		try {
			//shortArrays
			if(result[i].shortArrays!=null && result[i].shortArrays.length) {
				for (var j = 0; j < result[i].shortArrays.length; j++) {
					$("#user_container"+i).append("<div id=\"graph_s_"+i+"_"+j+"\" style=\"height: 200px;\"></div>");
					createLineChart('graph_s_'+i+"_"+j, result[i].shortArrays[j].descr, result[i].time, result[i].shortArrays[j].fs, false, null, result[i].shortArrays[j].array);
				}
			}
		} catch (e) { console.log('error add short data: ' + e); }
		
		
		try {
			//image
			if(result[i].pictureURL) {
				var lowerUrl = result[i].pictureURL.toLowerCase();
				if(lowerUrl.indexOf("http://")<0 && lowerUrl.indexOf("https://")<0) {
					result[i].pictureURL = "http://" + result[i].pictureURL; 
				}
				
//				result[i].pictureURL = "http://res.cloudinary.com/ecg4everybody/image/upload/v1459497562/zb37pqymbimhukinkkrt.png";
				
				if(lowerUrl.indexOf("jpg")>0 || lowerUrl.indexOf("png")>0 || lowerUrl.indexOf("jpeg")>0 || lowerUrl.indexOf("gif")>0) {
//					$("#user_container"+i).append("<a href=\"" + result[i].pictureURL + "\"  target=\"_blank\"><img src=\"" + result[i].pictureURL + "\"></img></a>");
					$("#user_container"+i).append("<a href=\"#img\"  class=\"pop\"> <img src=\"" + result[i].pictureURL + "\" alt = \""+result[i].note+"\" class=\"img-responsive\" /></a>");
//					console.log("<a href=\"#img\"  class=\"pop\"> <img src=\"" + result[i].pictureURL + "\" alt = \""+result[i].note+"\" class=\"img-responsive\"/></a>"); style=\"width:304px;height:228px;\" class=\"img-responsive\"
					//$("#user_container"+i).append("<br><br><img src=\"http://res.cloudinary.com/ecg4everybody/image/upload/v1459497562/zb37pqymbimhukinkkrt.png\" alt=\"Mountain View\">");
				}
				else {
					//TODO CHECK XFRAME SAMEORIGN
					$("#user_container"+i).append("<iframe frameborder=\"0\" style=\"height: 150px;text-align:left;\" src=\"" + result[i].pictureURL + "\"></iframe>");
					$("#user_container"+i).append("<a href=\"" + result[i].pictureURL + "\" target=\"_blank\">Follow user link</a>");
				}
				
			}
		} catch (e) { console.log('error add image data: ' + e); }
		
		$('.pop').on('click', function() {
    		console.log('click..');
			$('.imagepreview').attr('src', $(this).find('img').attr('src'));
			$( "#userMessage" ).html('<p><i class=\"fa fa-comment\"></i> ' + $(this).find('img').attr('alt') + '</p>'); 
			$('#imagemodal').modal('show');   
		});		    	
		
	}

	try {
		getMessages();
	} catch (e) { console.log('error add messages: ' + e); }
	
}

// messages
var currDataMessages = 0;
function getMessages() {
	$("#dataRow"+currDataMessages).append("<div id=\"messages"+dataArrary[currDataMessages].id+"\" class=\"col-xs-4\"	style=\"overflow: auto; height: 255px;\"></div>");

	$("#messages" + dataArrary[currDataMessages].id).append(
			(mobileAccess?"<small>crowdsourcing messages: </small>":"") + 
			"<button title=\"Participate, Provide information for user post\" type=\"button\" class=\"btn btn-md btn-circle btn-raised ripple-effect btn-info\"" + 
			" style=\"position:absolute; top:200px; right:10px;\" " +
			" onclick=\"initeAddMessage(" + dataArrary[currDataMessages].id + ")\">" +
	 		"<span class=\"glyphicon glyphicon-pencil\"></span></button>");
	
	if(dataArrary && currDataMessages<dataArrary.length) {
		$.get( "../e4emessage/list4data/" + dataArrary[currDataMessages].id + "/0/100", processGetMessage);
	}
}

function processGetMessage(result) {
	
	if(result && result.length) {
		
		for (var i = 0; i < result.length; i++) {
			$("#messages"+dataArrary[currDataMessages].id)
				.append("<div style=\"margin-top: .3em;border-bottom: 1px solid lightgrey;\">" +
				"<button type=\"button\" class=\"btn btn-xs glyphicon glyphicon-ok\" title=\"Rate as Correct\" onclick=\"correctRate(" + result[i].id + ")\"></button>" +
				"<div id=\"accRate" +result[i].id+ "\" class=\"message-rate\" title=\"Accumulated Rate for Answer (#High rates: "+result[i].plusRate+", #Low rates:"+result[i].minusRate+")\">" + 
					(result[i].plusRate-result[i].minusRate) + "</div>" +
				"<button type=\"button\" class=\"btn btn-xs glyphicon glyphicon-minus\" title=\"Rate as Incorrect\"  onclick=\"incorrectRate(" + result[i].id + ")\"></button>&nbsp; " +
				result[i].message + "</div>");
		}
		
	}
	
	currDataMessages++;
	if(dataArrary && currDataMessages<dataArrary.length) {
		getMessages();
	}
	else {
		// finished message data loading
	}
}

/// END GET DATA MESSAGES


/// RATE

var ratedMessageId = null;

function correctRate(messageId) {
	if(!loginUUID || !logedInuser) {
		showInfo("Login requested", "You need to be loged in in order to rate user's messages.");
		return;
	}
//	console.log("/e4erate/rate/" + messageId + "/true");
	$.get( "../e4erate/rate/" + messageId + "/true", processRate);
	ratedMessageId = messageId;
//	alert(messageId + ' +')
}

function processRate(result) {
	$.get( "../e4emessage/id/" + ratedMessageId, processRateGetAccRate); 
}

function processRateGetAccRate(result) {
	$("#accRate"+ratedMessageId).html((result.plusRate-result.minusRate));
	$("#accRate"+ratedMessageId).prop('title', "Accumulated Rate for Answer (#High rates: "+result.plusRate+", #Low rates:"+result.minusRate);
}

function incorrectRate(messageId) {
	if(!loginUUID || !logedInuser) {
		showInfo("Login requested", "<p>You need to be loged in in order to rate user's messages.</p>");
		return;
	}
	$.get( "../e4erate/rate/" + messageId + "/false", processRate);
	ratedMessageId = messageId;
}

/// END RATE


/// MESSAGE
var messageDataId;
function initeAddMessage(dataId) {
	
//	if(!loginUUID || !logedInuser) {
//		showInfo("<p>Login requested", "You need to be loged in in order to provide comment messages on user's data.</p>" +
//				"<p>Your e-mail will not be shown to other users.</p>");
//		return;
//	}
	
	messageDataId = dataId;
	
	$('#postMessageBtn').prop('disabled', false);
	$('#postMessageSpinner').hide();
	$('#comment').val('');
	
	$('#postMessageModal').modal('show');
	
	setTimeout(function (){
        $('#comment').focus();
    }, 1000);
	
//	$("#messages"+dataId).prepend("<p>Test</p>");
	
}

function postMessage() {
//	$('#postMessageModal').modal('hide');
//	$("#messages"+messageDataId).prepend("<p>"+$('#comment').val()+"</p>");
	
	if(!$('#comment').val().length) {
		$('#postMessageModal').modal('hide');
		showInfo('Error', '<p>Comment message has no content</p>');
		return;
	} 
	
	$('#postMessageBtn').prop('disabled', true);
	$('#postMessageSpinner').show();
	
	var message = {
		"message" : $('#comment').val(),
		"commentedDataId" : messageDataId
	};
	$.post( "../e4emessage/", JSON.stringify(message), processPostMessage);
}

function processPostMessage(result) {
//	$("#messages"+messageDataId).prepend("<p>"+result+"</p>");
	
	if ( result.indexOf("ok") > -1 ) {
		$('#postMessageModal').modal('hide');
		showInfo("Thanks", "<p>Thank you for your contribution.</p> " +
				"<p>Your message will apear on the Crowdsourcing portal after our physician review.</p>")
	}
	
}
/// END MESSAGE

/// ALERT MODAL
function showInfo(title, body) {
	$('#infoModalTitle').html(title);
	$('#infoModalBody').html(body);
	$('#infoModal').modal('show');
	setTimeout(function (){
        $('#closeButton').focus();
    }, 1000);
}
/// END ALERT MODAL

/// RISE MESSAGE on CS
function riseMessage() {
	if(!loginUUID || !logedInuser) {
		showInfo("<p>Login requested", "You need to be loged in in order to rise a new message data on the Crowdsourcing portal.</p>" +
				"<p>Your e-mail will not be shown to other users.</p>");
		return;
	}
	$('#riseMessageSpinner').hide();
	
	if(!loginUUID || !logedInuser) {
		$('#add_email').show();
	}
	else {
		$('#add_email').hide();
	}
	
	$('#riseMessage').modal('show');
	setTimeout(function (){
        $('#csMessage').focus();
    }, 1000);
	
}

function createCrowdsourcingMessage() {
	var email = null;
	if(!$('#csMessage').val().length && !$('#csUrl').val().length ) {
		$('#riseMessage').modal('hide');
		showInfo('Error', '<p>Crowdsourcing message has no content</p>');
		return;
	} 
	else if ( (!loginUUID || !logedInuser) & !$('#cs_message_email').val().length)  {
		$('#riseMessage').modal('hide');
		showInfo('Error', '<p>e-mail address is emptyt</p>');
		return;		
	}

	if(!loginUUID || !logedInuser) {
		email = $('#cs_message_email').val();
	}
	
	setCookie('email', $('#cs_message_email').val(), 365);
	
	var data = { "userId": logedInuser?logedInuser.id:null,
			"note": $('#csMessage').val(),
			"pictureURL" : $('#csUrl').val()};
	
	$.post( "../e4edata/" + (email?"?email="+email:""), JSON.stringify(data), processPostData);		
	$('#riseMessageSpinner').show();
}

function processPostData(result) {
//	$("#messages"+messageDataId).prepend("<p>"+result+"</p>");
	
	if ( result.indexOf("ok") > -1 ) {
		$('#riseMessage').modal('hide');
		showInfo("Thanks", "<p>Thank you for your contribution.</p> " +
				"<p>We hope you will get many valuable crowdsourcing messages.</p> " +
				"<p>Your message will apear on the Crowdsourcing portal after internal review.</p>")
	}
	
}
/// END RISE MESSAGE on CS