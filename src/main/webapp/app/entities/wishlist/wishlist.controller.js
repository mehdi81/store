(function() {
    'use strict';

    angular
        .module('storeApp')
        .controller('WishlistController', WishlistController);

    WishlistController.$inject = ['$scope', '$state', 'Wishlist'];

    function WishlistController ($scope, $state, Wishlist) {
        var vm = this;
        
        vm.wishlists = [];

        loadAll();

        function loadAll() {
            Wishlist.query(function(result) {
                vm.wishlists = result;
            });
        }
    }
})();
