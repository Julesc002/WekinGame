import React from 'react';
import AjoutWiki from '../components/AjoutWiki';
import DisableBackgroundWiki from '../components/DisableBackgroundWiki';
import Header from '../components/Header';

const PageAjoutWiki = () => {
    return (
        <div>
            <DisableBackgroundWiki />
            <Header />
            <AjoutWiki />
        </div>
    );
};

export default PageAjoutWiki;