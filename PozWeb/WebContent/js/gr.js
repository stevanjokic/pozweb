function createLineChart(renderToHTMLElement, graphTitle, startTime, sampleInterval, yAxisEnabled, yAxixTitle, values) {
	
	var yParams = graphTitle ? graphTitle.split(";") : null;
//	alert(startTime + ' -> ' + moment(startTime, 'YYYY-MM-DD hh:mm:ss.SSS') );
	
	var chartS = new Highcharts.StockChart(
			{
				
			    chart: {
			        renderTo: renderToHTMLElement,	        
			    },
			        
			    title: {
					text: yParams ? yParams[0] : null
				},
				
				yAxis : {
					title : {
						text : yParams && yParams.length>1 ? yParams[1] : null
					},
					allowDecimals: false,
					min: yParams && yParams.length>2 ? yParams[2] : null,
		            max: yParams && yParams.length>3 ? yParams[3] : null,
	                labels: {
	                    enabled: yAxisEnabled
	                }
	            },
	            
	            rangeSelector: {
	                enabled: false
	            },
	            
	            navigator: {
	                height: 10
	            },
			    
				series: [{
					data: values, 
//			        pointStart: startTime!=null ? new Date(moment(startTime, 'YYYY-MM-DD hh:mm:ss.SSS')) : null, //
//					pointStart: startTime!=null ? moment(startTime).format('YYYY-MM-DD hh:mm:ss.SSS').toDate() : null,
//					pointStart: startTime!=null ? moment(startTime, 'YYYY-MM-DD hh:mm:ss.SSS').toDate() : null,
					pointStart:	Number(moment(startTime, 'YYYY-MM-DD hh:mm:ss.SSS')),
			        pointInterval: sampleInterval ? 1000/sampleInterval : null,
			        tooltip: {
			        	valueDecimals: 0		        	
			        }
			    }]
		
			}
	);
	
	return chartS;
}


