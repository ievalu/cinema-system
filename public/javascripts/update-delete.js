function deleteRow(deleteUrl, homeUrl){
	$.ajax({
	    url: deleteUrl,
	    type: 'DELETE',
	    success: function(result) {
	        window.location = homeUrl;
	    }
	});
}