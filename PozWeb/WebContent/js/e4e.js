function carouselNormalization() {
	var items = $('#carousel-wrapper .item'), // grab all slides
	heights = [], // create empty array to store height values
	shortest; // create variable to make note of the shortest slide

//	alert(items.length);
	
	if (items.length) {
		function normalizeHeights() {
			items.each(function() { // add heights to array
				heights.push(jQuery(this).height());
			});
			shortest = Math.max.apply(null, heights); // cache largest value
			items.each(function() {
				jQuery('.carousel-inner').css('height', shortest + 'px').css(
						'overflow', 'hidden');

			});
		}
		;
		normalizeHeights();

		jQuery(window).on('resize orientationchange', function() {
			shortest = 0, heights.length = 0; // reset vars
			items.each(function() {
				jQuery('.carousel-inner').css('height', '0'); // reset
																// min-height
			});
			normalizeHeights(); // run it again
		});
	}
}


/*$(document).ready(function(){
  // Add smooth scrolling to all links in navbar + footer link
  $(".navbar a, footer a[href='#myPage']").on('click', function(event) {

    // Prevent default anchor click behavior
    event.preventDefault();

    // Store hash
    var hash = this.hash;

    // Using jQuery's animate() method to add smooth page scroll
    // The optional number (900) specifies the number of milliseconds it takes to scroll to the specified area
    $('html, body').animate({
      scrollTop: $(hash).offset().top
    }, 900, function(){
   
      // Add hash (#) to URL when done scrolling (default click behavior)
      window.location.hash = hash;
    });
  });
  
  // Slide in elements on scroll
  $(window).scroll(function() {
    $(".slideanim").each(function(){
      var pos = $(this).offset().top;

      var winTop = $(window).scrollTop();
        if (pos < winTop + 600) {
          $(this).addClass("slide");
        }
    });
  });
  
//  $('.myCarousel').carousel();
  
  carouselNormalization();

//  $('#carousel-inner .item').carouselHeights();

  
  
//  alert('test' + $('#item2').height() + ' ' + $('#item1').height());
  
})*/

