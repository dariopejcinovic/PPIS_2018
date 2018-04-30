import React, { Component } from 'react';
import {Switch, Route} from 'react-router-dom';

import ListaIncidenata from './ListaIncidenata';
import Incident from './Incident';

class Incidenti extends Component{
    render(){
        return(
            <Switch>
                <Route exact path = {this.props.match.url} component={ListaIncidenata}/>
                <Route path ={'${this.props.match.url}/:id'} component={Incident}/>
            </Switch>
        );
    }
}

export default Incidenti;