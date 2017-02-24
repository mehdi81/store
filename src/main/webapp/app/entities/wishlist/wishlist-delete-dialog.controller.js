(function() {
    'use strict';

    angular
        .module('storeApp')
        .controller('WishlistDeleteController',WishlistDeleteController);

    WishlistDeleteController.$inject = ['$uibModalInstance', 'entity', 'Wishlist'];

    function WishlistDeleteController($uibModalInstance, entity, Wishlist) {
        var vm = this;

        vm.wishlist = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Wishlist.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
