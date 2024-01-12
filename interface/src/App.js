import React from 'react';
import { Route, Routes } from 'react-router-dom';

import ConnexionAuCompte from './components/ConnexionCompte';
import CreationCompte from './components/CreationCompte';
import InfoCompte from './components/InfoCompte';
import Layout from './components/Layout';
import Accueil from './pages/Accueil';
import Categorie from './pages/Categorie';
import Entree from './pages/Entree';
import PageAdministrerWiki from './pages/PageAdministrerWiki';
import PageAffichageAllWikis from './pages/PageAffichageAllWikis';
import PageAjoutEntree from './pages/PageAjoutEntree';
import PageAjoutWiki from './pages/PageAjoutWiki';
import PageErreur from './pages/PageErreur';
import PageModificationBackgroundImage from './pages/PageModificationBackgroundImage';
import PageModificationEntree from './pages/PageModificationEntree';
import PageModifierCategorie from './pages/PageModifierCategorie';
import Wiki from './pages/Wiki';

const App = () => {

  return (
    <Layout>
      <Routes>
        <Route path='*' element={<PageErreur />} />
        <Route path='/' element={<Accueil />} />
        <Route path="/account/new" element={<CreationCompte/>} />
        <Route path="/entree/:id" element={<Entree />} />
        <Route path="/categorie/:id/:nom" element={<Categorie />} />
        <Route path="/wiki/:id/" element={<Wiki />} />
        <Route path="/account/info" element={<InfoCompte/>} />
        <Route path="/account/connect" element={<ConnexionAuCompte/>}/>
        <Route path="/wiki/:id/ajoutEntree" element={<PageAjoutEntree />} />
        <Route path="/createWiki/:nomParDefaut" element={<PageAjoutWiki />} />
        <Route path="/createWiki" element={<PageAjoutWiki />} />
        <Route path="/allWikis" element={<PageAffichageAllWikis />} />
        <Route path="/wiki/:wikiId/entry/:entreeId/update" element={<PageModificationEntree />} />
        <Route path='/wiki/:id/category/:oldCategoryName/update' element={<PageModifierCategorie />}/>
        <Route path="/wiki/:wikiId/admin" element={<PageAdministrerWiki/>} />
        <Route path="/wiki/:wikiId/background" element={<PageModificationBackgroundImage />} />
      </Routes>
    </Layout>
  );
};

export default App;