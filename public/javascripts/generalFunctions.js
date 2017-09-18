function confirmAction(){
    if(confirm("Are you sure want to delete")){
        console.log("User Confirmed");
        return true;
    }
    else{
        console.log("User canceled this operation");
        return false;
    }
}