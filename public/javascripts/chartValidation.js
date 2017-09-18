
    function checkAggregationUsage(activatedObj){
      var secondAggregator;
      var field;
      if(activatedObj.id=="aggregationY"){
        secondAggregator = document.getElementById("aggregationX");
        field = document.getElementById("fieldY");
      }
      else{
        secondAggregator = document.getElementById("aggregationY");
        field = document.getElementById("fieldX");
      }
      if(secondAggregator.value!="None"){
        // alert("Please remove aggregation from others");
        secondAggregator.value="None";
      }
      checkAggregationFields(field);
      // alert(secondAggregator.id);
    }
    function checkAggregationFields(activatedObj){
      var aggregator;
      if(activatedObj.value=="date"||activatedObj.value=="sentiment"){
        if(activatedObj.id=="fieldX"){
          aggregator = document.getElementById("aggregationX");
        }
        else{
          aggregator = document.getElementById("aggregationY"); 
        }
        if(aggregator.value!="None"){
          alert("This field cannot be aggregated");
          aggregator.value="None";
        }
      }
    }
    function checkFieldsUnique(activatedObj){
      var secondField;
      if(activatedObj.id=="fieldX"){
        secondField = document.getElementById("fieldY");
      }
      else{
        secondField = document.getElementById("fieldX"); 
      }
      if(activatedObj.value=="date"||activatedObj.value=="sentiment"){
        if(secondField.value=="date"||secondField.value=="sentiment"){
          secondField.value="retweetcount";
        }
      }
      else{
        if(secondField.value=="retweetcount"||secondField.value=="sentimentvalue"){
          secondField.value="sentiment";
        } 
      }
    }
    function isThereAnyAggregation(){
      var groupBy = document.getElementById("groupBy").value;  
      var aggregatorX = document.getElementById("aggregationX");
      var aggregatorY = document.getElementById("aggregationY");
      if(groupBy!="None"){       
        if(aggregatorY.value=="None"&&aggregatorX.value=="None"){
          alert("There Should be atleast one agrregation to group by");
          return false;
        } 
      }
      if(aggregatorY.value!="None"||aggregatorX.value!="None"){
        if(groupBy=="None"){
          alert("There should be one group by field");
          return false;
        }
      }
    }