(function() {
    'use strict';

    angular
        .module('storeApp')
        .controller('WishlistController', WishlistController);

    WishlistController.$inject = ['Wishlist'];

    function WishlistController(Wishlist) {

        var vm = this;

        vm.wishlists = [];

        loadAll();

        function loadAll() {
            Wishlist.query(function(result) {
                vm.wishlists = result;
                vm.searchQuery = null;
            });
        }
    }
})();
