import axios from "axios";
import { useState } from "react";
import { useParams } from "react-router-dom";
import { API_URL } from '../config';

function AjoutCategorie() {
    const { id } = useParams();
    const [recherche, setRecherche] = useState('');
    const [errorMessage, setErrorMessage] = useState('');

    const majRecherche = (e) => {
        setRecherche(e.target.value);
    };

    function handleAddCategory() {

        const requestData = {
            nom: recherche
        };
        if (recherche.trim().length === 0) {
            setErrorMessage("Veuillez remplir le champ");
        } else {
            setErrorMessage("");
            axios.patch( API_URL+'/wiki/'+ id + '/category/create', requestData).then((response) => {
                if (response.status === "200") {
                    window.location.reload();
                } else if (response.status === "409") {
                    alert('La catégorie existe déjà');
                }
            }).catch((error) => {
                console.error("Erreur lors de l'ajout de la catégorie :", error);
            });
        }
    }

    return (
        <div>
            <input type="text" placeholder="Insérer nom" onChange={majRecherche} />
            <button onClick={handleAddCategory}>Ajouter</button>
            <p>{errorMessage}</p>
        </div>
    );
}

export default AjoutCategorie;