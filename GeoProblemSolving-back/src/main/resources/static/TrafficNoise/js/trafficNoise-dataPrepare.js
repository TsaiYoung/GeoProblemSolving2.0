var userName = "", userId="", currentResources = [];
window.onpageshow = function (e) {

    basicCollabComponent()
    getStepInfo();

    function getStepInfo() {
        if (componentStatus) {
            // 获取数据
            userId = userInfo.userId;
            userName = userInfo.name;
            currentResources = resources;
            activityInfo = activityInfo;
            isLogin();

        } else {
            setTimeout(function () {
                getStepInfo();
            }, 1000);
        }
    }
}

function showIndex() {
    isLogin();
}

function onLoginClick() {
    isLogin();
}

function onLogoutClick() {
    // $.ajax({
    //     type: "post",
    //     url: "/GeoProblemSolving/logoutServlet",
    //     data: {
    //         "userName": userName
    //     },
    //     success: function (resp) {
    //         $("#login").show();
    //         $("#userInfo").hide();
    //         $("#content-iframe").attr("src", "./login.html");
    //         $("#operate").hide();
    //     }
    // });
}

function onRegisterClick() {
    $("#content-iframe").attr("src", "./register.html");
}

function isLogin() {
    // $("#userInfo").show();
    // $("#userName").text(userName);
    $("#content-iframe").attr("src", "./RLS90-dataPrepare.html");
    // $("#content-iframe").get(0).contentWindow.userId = userId;
    $("#operate").show();
    // $.ajax({
    //     type: "post",
    //     url: "/GeoProblemSolving/userStatusServlet",
    //     success: function (resp) {
    //         var result = JSON.parse(resp);
    //         if (result.isLogin == 1) {
    //             userName = result.userName;
    //             userId = result.userId;
    //             $("#userName").text(userName);
    //
    //             $("#login").hide();
    //             $("#userInfo").show();
    //             $("#content-iframe").attr("src", "./RLS90-dataPrepare.html");
    //             $("#content-iframe").get(0).contentWindow.userId = result.userId;
    //             $("#operate").show();
    //         }
    //         else {
    //             $("#operate").hide();
    //             $("#login").show();
    //             $("#userInfo").hide();
    //             $("#content-iframe").attr("src", "./login.html");
    //         }
    //     }
    // });
}

window.addEventListener('message', function (e) {
    // console.log(e.data);
    var from = e.data.from;
    switch (from) {
        case "login.html":
            if (e.data.TODO == "login") {
                $("#userName").text(e.data.userName);

                $("#login").hide();
                $("#userInfo").show();
                $("#content-iframe").attr("src", "./RLS90-dataPrepare.html");
            }
            break;
        case "register.html":
            break;
        case "RLS90-dataPrepare.html":
            break;
    }
})

function selectData(dataType) {
    $("#content-iframe").get(0).contentWindow.selectData(dataType);
}

function exportData(dataType) {
    $("#content-iframe").get(0).contentWindow.exportData(dataType);
}

$(function () {
    $(window).resize(function () {
        if (document.body.clientWidth < 900) {
            window.resizeTo(document.body.clientWidth, 300);
        }
    })

})