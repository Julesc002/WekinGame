import React from 'react';
import AffichagesAllWikis from '../components/AffichagesAllWikis';
import DisableBackgroundWiki from '../components/DisableBackgroundWiki';
import Header from '../components/Header';

const PageAffichageAllWikis = () => {
    return (
        <div>
            <DisableBackgroundWiki />
            <Header />
            <AffichagesAllWikis />
        </div>
    );
};

export default PageAffichageAllWikis;
