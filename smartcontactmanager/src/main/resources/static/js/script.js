console.log("this is script file");

const toggleSidebar = () => {
    if ($(".sidebar").is(":visible")) {
        // close it
        $(".sidebar").css("display", "none");
        $(".content").css("margin-left", "0%");
    } else {
        // show it
        $(".sidebar").css("display", "block");
        $(".content").css("margin-left", "20%");
    }
};

const search = () => {
    let query = $("#search-input").val();

    if (query === "") {
        $(".search-result").hide();
    } else {
        console.log("Searching for:", query);

        // ✅ Corrected URL using backticks
        let url = `http://localhost:8080/search/${query}`;

        fetch(url)
            .then((response) => response.json())
            .then((data) => {
                console.log(data);

                // ✅ Build dynamic HTML properly
                let text = `<div class='list-group'>`;

                data.forEach((contact) => {
                    text += `<a href='/user/${contact.cId}/contact' class='list-group-item list-group-item-action'>${contact.name}</a>`;
                });

                text += `</div>`;

                // ✅ Update search result
                $(".search-result").html(text);
                $(".search-result").show();
            })
            .catch((error) => {
                console.error("Error fetching search results:", error);
            });
    }
};
